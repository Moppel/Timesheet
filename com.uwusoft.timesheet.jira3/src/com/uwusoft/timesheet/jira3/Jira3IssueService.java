package com.uwusoft.timesheet.jira3;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.atlassian.core.util.collection.EasyList;
import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.LoginDialog;
import com.uwusoft.timesheet.dialog.PreferencesDialog;
import com.uwusoft.timesheet.extensionpoint.IssueService;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.util.SecurePreferencesManager;

public class Jira3IssueService implements IssueService {
	public static final String PREFIX = "jira3.issue.";
	
    private String message;
    private XmlRpcClient rpcClient;
    private String loginToken;
    
	public Jira3IssueService() throws CoreException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        if (StringUtils.isEmpty(preferenceStore.getString(PREFIX + SubmissionService.URL))
        		|| StringUtils.isEmpty(preferenceStore.getString(PREFIX + StorageService.USERNAME))) {
        		Display.getDefault().syncExec(new Runnable() {
            		@Override
            		public void run() {
            			PreferencesDialog preferencesDialog;
                    	do
                    		preferencesDialog = new PreferencesDialog(Display.getDefault(), "com.uwusoft.timesheet.jira3.Jira3PreferencePage");
                        while (preferencesDialog.open() != Dialog.OK);
            		}
            	});
        	}
		// Initialize RPC Client
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	    try {
			config.setServerURL(new URL(preferenceStore.getString(PREFIX + SubmissionService.URL) + "rpc/xmlrpc"));
		} catch (MalformedURLException e) {
	        throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, e.getMessage(), null));
		}
		rpcClient = new XmlRpcClient();
		rpcClient.setConfig(config);
		// see http://singztechmusings.wordpress.com/2010/12/09/xml-rpc-over-ssl/
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
		 
				public void checkClientTrusted(X509Certificate[] certs,	String authType) {
					// Trust always
				}
		 
				public void checkServerTrusted(X509Certificate[] certs,	String authType) {
					// Trust always
				}
			}
		};
		 
		try {
			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			// Create empty HostnameVerifier
			HostnameVerifier hv = new HostnameVerifier() {
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			};
			
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
		} catch (NoSuchAlgorithmException e) {
	        throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, e.getMessage(), null));
		} catch (KeyManagementException e) {
	        throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, e.getMessage(), null));
		}		
		login();
    }

	private void login() throws CoreException {
		boolean lastSuccess = true;
        do lastSuccess = authenticate(lastSuccess);
       	while (!lastSuccess);
	}
	
	private void logout() {
		Vector<String> loginTokenVector = new Vector<String>(1);
        loginTokenVector.add(loginToken);
		try {
	        Boolean bool = (Boolean) rpcClient.execute("jira1.logout", loginTokenVector);
	        //System.out.println("Logout successful: " + bool);
	    } catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}

    private boolean authenticate(boolean lastSuccess) throws CoreException {
		final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		final SecurePreferencesManager secureProps = new SecurePreferencesManager("Jira");
		final Vector<String> loginParams = new Vector<String>(2);
    	final String userName = preferenceStore.getString(PREFIX + StorageService.USERNAME);
    	final String password = secureProps.getProperty(PREFIX + StorageService.PASSWORD);
    	if (lastSuccess && !StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password)) {
    		loginParams.add(userName);
    		loginParams.add(password);
    		try {
				loginToken = (String) rpcClient.execute("jira1.login", loginParams);
			} catch (XmlRpcException e) {
		        throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, e.getMessage(), null));
			}
        	return true;
    	}
    	
    	final Display display = Display.getDefault();
    	display.syncExec(new Runnable() {
			@Override
			public void run() {
		    	LoginDialog loginDialog = new LoginDialog(display, "Jira Log in", message, userName, password);
				if (loginDialog.open() == Dialog.OK) {
		        	message = null;
		    		// Login and retrieve logon token
		    		loginParams.add(loginDialog.getUser());
		    		loginParams.add(loginDialog.getPassword());
		    		try {
						loginToken = (String) rpcClient.execute("jira1.login", loginParams);
					} catch (XmlRpcException e) {
						message = e.getMessage();
					}
		        	
		    		preferenceStore.setValue(PREFIX + StorageService.USERNAME, loginDialog.getUser());
		        	if (loginDialog.isStorePassword())
		        		secureProps.storeProperty(PREFIX + StorageService.PASSWORD, loginDialog.getPassword());
		        	else
		        		secureProps.removeProperty(PREFIX + StorageService.PASSWORD);
				}
				else message = "Not logged in";
			}
    	});
    	if (message == null) return true;
        throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, null));
    }    

	public String createIssue(Hashtable<String, Serializable> struct) {
		try {
			login();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        // Constants for issue creation
        struct.put("assignee", preferenceStore.getString(PREFIX + StorageService.USERNAME));
        struct.put("reporter", preferenceStore.getString(PREFIX + StorageService.USERNAME));

		/*try {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Map issueMap = (Map) rpcClient.execute("jira1.createIssue", new Vector(EasyList.build(loginToken, struct)));
	        return (String) issueMap.get("key");		
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}*/
        logout();
		return "";
	}
    
	@Override
	public Object[] getProjects() {
		try {
			login();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		Vector<String> searchVector = new Vector<String>(1);
		searchVector.add(loginToken);
		try {
			Object[] retValue = (Object[]) rpcClient.execute("jira1.getProjectsNoSchemes", searchVector);
			logout();
			return retValue; 
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}
	}
    
	@Override
    public Object[] getComponents(String projectKey) {
		try {
			login();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		Vector<String> searchVector = new Vector<String>(2);
		searchVector.add(loginToken);
		searchVector.add(projectKey);
		try {
			Object[] retValue = (Object[]) rpcClient.execute("jira1.getComponents", searchVector);
			logout();
			return retValue;
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}
    }
	
	@Override
    public Object[] getSavedFilters() {
		try {
			login();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		Vector<String> searchVector = new Vector<String>(1);
		searchVector.add(loginToken);
		try {
			Object[] retValue = (Object[]) rpcClient.execute("jira1.getSavedFilters", searchVector);
			logout();
			return retValue;
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    protected Object[] getSubTaskIssueTypesForProject(String projectId) {    	
		try {
			login();
		} catch (CoreException e) {
			e.printStackTrace();
		}
 		Vector<String> searchVector = new Vector<String>(2);
 		searchVector.add(loginToken);
 		searchVector.add(projectId);
 		try {
 			Object[] retValue = (Object[]) rpcClient.execute("jira1.getSubTaskIssueTypesForProject", searchVector);
 			logout();
 			return retValue;
 		} catch (XmlRpcException e) {
 			e.printStackTrace();
 			return null;
 		}
    }
    
    protected Object[] getIssuesFromFilter(String filterId) {
		try {
			login();
		} catch (CoreException e) {
			e.printStackTrace();
		}
    	Vector<String> searchVector = new Vector<String>(2);
		searchVector.add(loginToken);
		searchVector.add(filterId);
 		try {
 			Object[] retValue = (Object[]) rpcClient.execute("jira1.getIssuesFromFilter", searchVector);
 			logout();
 			return retValue;
 		} catch (XmlRpcException e) {
 			e.printStackTrace();
 			return null;
 		}
    }
}
