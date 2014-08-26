/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * RaiMan 2013
 */
package org.sikuli.setup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import javax.swing.JFrame;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.SplashFrame;
import org.sikuli.basics.PreferencesUser;
import org.sikuli.basics.ResourceLoader;
import org.sikuli.basics.Settings;
import org.sikuli.script.Sikulix;

public class RunSetup {

  private static String downloadedFiles;
  private static boolean runningUpdate = false;
	private static boolean isUpdateSetup = false;
	private static boolean runningfromJar = true;
	private static boolean noSetup = false;
	private static boolean noSetupSilent = false;
	private static boolean backUpExists = false;
	private static String workDir;
	private static String uhome;
	private static String logfile;
	private static String version = Settings.getVersionShort();
//TODO wrong if version number parts have more than one digit
	private static String minorversion = Settings.getVersionShort().substring(0,5);
	private static String majorversion = Settings.getVersionShort().substring(0,3);
	private static String updateVersion;
	private static String downloadSetup;
	private static String downloadIDE = version + "-1.jar";
	private static String downloadJava = version + "-2.jar";
	private static String downloadRServer = version + "-3.jar";
	private static String downloadJython = version + "-4.jar";
	private static String downloadJRuby = version + "-5.jar";
	private static String downloadJRubyAddOns = version + "-6.jar";
	private static String downloadMacAppSuffix = "-9.jar";
	private static String downloadMacApp = minorversion + downloadMacAppSuffix;
	private static String downloadTessSuffix = "-8.jar";
	private static String downloadTess = minorversion + downloadTessSuffix;
	private static String localJava = "sikulixapi.jar";
	private static String localIDE = "sikulix.jar";
	private static String localMacApp = "sikulixmacapp.jar";
	private static String localMacAppIDE = "SikuliX-IDE.app/Contents/sikulix.jar";
	private static String folderMacApp = "SikuliX-IDE.app";
	private static String folderMacAppContent = folderMacApp + "/Contents";
  private static String setupName = "sikulixsetup-" + version;
	private static String localSetup =  setupName + ".jar";
	private static String localUpdate = "sikulixupdate";
	private static String localTess = "sikulixtessdata.jar";
	private static String localRServer = "sikulixremoteserver.jar";
	private static String localJython = "sikulixjython.jar";
	private static String localJRuby = "sikulixjruby.jar";
	private static String localJRubyAddOns = "sikulixjrubyaddons.jar";
	private static String runsikulix = "runsikulix";
	private static String localLogfile;
	private static SetUpSelect winSU;
	private static JFrame winSetup;
	private static boolean getIDE, getJython, getJava;
  private static boolean getRServer = false;
	private static boolean forAllSystems = false;
	private static boolean getTess = false;
  private static boolean getJRuby = false;
  private static boolean getJRubyAddOns = false;
	private static String localJar;
	private static boolean test = false;
	private static boolean isUpdate = false;
	private static boolean isBeta = false;
	private static String runningJar;
	private static List<String> options = new ArrayList<String>();
	private static JFrame splash = null;
	private static String me = "RunSetup";
	private static String mem = "...";
	private static int lvl = 2;
	private static String msg;
	private static boolean shouldPackLibs = true;
	private static long start;
	private static boolean runningSetup = false;
	private static boolean generallyDoUpdate = false;
	private static String timestampBuilt = Settings.SikuliVersionBuild;
  private static int optionsSize;
  private static boolean logToFile = true;

	//<editor-fold defaultstate="collapsed" desc="new logging concept">
	private static void log(int level, String message, Object... args) {
		Debug.logx(level, me + ": " + mem + ": " + message, args);
	}

	private static void log0(int level, String message, Object... args) {
		Debug.logx(level, me + ": " + message, args);
	}

	private static void log1(int level, String message, Object... args) {
		String sout;
		String prefix = level < 0 ? "error" : "debug";
		if (args.length != 0) {
			sout = String.format("[" + prefix + "] " + message, args);
		} else {
			sout = "[" + prefix + "] " + message;
		}
		Debug.logx(level, me + ": " + message, args);
    if (logToFile) {
      System.out.println(sout);
    }
	}
//</editor-fold>

	public static void main(String[] args) throws IOException {
		mem = "main";

		PreferencesUser prefs = PreferencesUser.getInstance();
		boolean prefsHaveProxy = false;

		if (Settings.SikuliVersionBetaN > 0 && Settings.SikuliVersionBetaN < 99) {
			updateVersion = String.format("%d.%d.%d-Beta%d",
							Settings.SikuliVersionMajor, Settings.SikuliVersionMinor, Settings.SikuliVersionSub,
							1 + Settings.SikuliVersionBetaN);
		} else if (Settings.SikuliVersionBetaN < 1) {
			updateVersion = String.format("%d.%d.%d",
							Settings.SikuliVersionMajor, Settings.SikuliVersionMinor,
							1 + Settings.SikuliVersionSub);
		} else {
			updateVersion = String.format("%d.%d.%d",
							Settings.SikuliVersionMajor, 1 + Settings.SikuliVersionMinor, 0);
		}

		options.addAll(Arrays.asList(args));
    optionsSize = options.size();

		//<editor-fold defaultstate="collapsed" desc="options return version">
		if (args.length > 0 && "build".equals(args[0])) {
			System.out.println(Settings.SikuliVersionBuild);
			System.exit(0);
		}

		if (args.length > 0 && "pversion".equals(args[0])) {
			System.out.println(Settings.SikuliProjectVersion);
			System.exit(0);
		}

		if (args.length > 0 && "uversion".equals(args[0])) {
			System.out.println(Settings.SikuliProjectVersionUsed);
			System.exit(0);
		}

		if (args.length > 0 && "version".equals(args[0])) {
			System.out.println(Settings.getVersionShort());
			System.exit(0);
		}

		if (args.length > 0 && "minorversion".equals(args[0])) {
			System.out.println(minorversion);
			System.exit(0);
		}

		if (args.length > 0 && "majorversion".equals(args[0])) {
			System.out.println(majorversion);
			System.exit(0);
		}

		if (args.length > 0 && "updateversion".equals(args[0])) {
			System.out.println(updateVersion);
			System.exit(0);
		}
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="other options">
    if (args.length > 0 && "test".equals(args[0])) {
      test = true;
      options.remove(0);
      if (options.isEmpty()) {
        getIDE = true;
        getJython = true;
        getJava = true;
      } else {
        if ("jruby".equals(options.get(0))) {
          options.remove(0);
          getIDE = true;
          getJRuby = true;
        }
      }
    }

    if (options.size() > 0 && "noSetup".equals(options.get(0))) {
      noSetup = true;
      options.remove(0);
    }

    if (options.size() > 0 && "update".equals(options.get(0))) {
      runningUpdate = true;
      options.remove(0);
    }

    if (options.size() > 0 && "updateSetup".equals(options.get(0))) {
      isUpdateSetup = true;
      options.remove(0);
    }
    //</editor-fold>

		runningJar = FileManager.getJarName();

		if (runningJar.isEmpty()) {
			popError("error accessing jar - terminating");
			System.exit(999);
		}
		if (runningJar.startsWith("sikulixupdate")) {
			runningUpdate = true;
		}

		if (runningUpdate) {
			localLogfile = "SikuliX-" + version + "-UpdateLog.txt";
		} else {
			localLogfile = "SikuliX-" + version + "-SetupLog.txt";
		}

		//<editor-fold defaultstate="collapsed" desc="option makeJar">
		String baseDir = null;
		if (options.size() > 0 && options.get(0).equals("makeJar")) {
			options.remove(0);
			String todo, jarName, folder;
			while (options.size() > 0) {
				todo = options.get(0);
				options.remove(0);
				//***
				// unpack or pack a jar to/from a folder
				//***
				if (todo.equals("unpack") || todo.equals("pack")) {
					if (options.size() < 1) {
						log0(-1, todo + ": invalid options! need a jar");
						System.exit(0);
					}
					jarName = options.get(0);
					options.remove(0);
					if (jarName.endsWith(".jar")) {
						if (options.size() < 1) {
							log0(-1, todo + ": invalid options! need a folder");
							System.exit(0);
						}
						folder = options.get(0);
						options.remove(0);
					} else {
						folder = jarName;
						jarName += ".jar";
					}
					if (options.size() > 0) {
						baseDir = options.get(0);
						options.remove(0);
						if (!new File(baseDir).isAbsolute()) {
							baseDir = new File(workDir, baseDir).getAbsolutePath();
						}
					}
					if (!new File(folder).isAbsolute()) {
						if (baseDir == null) {
							baseDir = workDir;
						}
						folder = new File(baseDir, folder).getAbsolutePath();
					}
					if (!new File(jarName).isAbsolute()) {
						if (baseDir == null) {
							baseDir = workDir;
						}
						jarName = new File(baseDir, jarName).getAbsolutePath();
					}
					if (todo.equals("unpack")) {
						log0(3, "requested to unpack %s \nto %s", jarName, folder);
						FileManager.unpackJar(jarName, folder, true);
					} else {
						String jarBack = jarName.substring(0, jarName.length() - 4) + "-backup.jar";
						try {
							FileManager.xcopy(jarName, jarBack);
						} catch (IOException ex) {
							log(-1, "could not create backUp - terminating");
							System.exit(0);
						}
						log0(3, "requested to pack %s \nfrom %s\nbackup to: %s", jarName, folder, jarBack);
						FileManager.packJar(folder, jarName, "");
					}
					log0(3, "completed!");
					continue;
					//***
					// build a jar by combining other jars (optionally filtered) and/or folders
					//***
				} else if (todo.equals("buildJar")) {
					// build jar arg0
					if (options.size() < 2) {
						log0(-1, "buildJar: invalid options!");
						System.exit(0);
					}
					jarName = options.get(0);
					options.remove(0);
					folder = options.get(0);
					options.remove(0);
					log0(3, "requested to build %s to %s", jarName, folder);
					// action
					log0(3, "completed!");
					continue;
				} else {
					log0(-1, "makejar: invalid option: " + todo);
					System.exit(0);
				}
			}
			System.exit(0);
		}
    //</editor-fold>

		if (options.size() > 0) {
			popError("invalid command line options - terminating");
			System.exit(999);
		}

		//<editor-fold defaultstate="collapsed" desc="general preps">
		Settings.runningSetup = true;
		Settings.LogTime = true;
		Debug.setDebugLevel(3);

		uhome = System.getProperty("user.home");
		workDir = FileManager.getJarParentFolder();
		if (workDir.startsWith("N")) {
			runningfromJar = false;
		}
		workDir = workDir.substring(1);

    if (!runningfromJar || runningJar.endsWith("-plain.jar")) {
      if (noSetup) {
        log(3, "creating Setup folder - not running setup");
      } else {
        log(3, "have to create Setup folder before running setup");
      }
      if (!createSetupFolder("")) {
        log(-1, "createSetupFolder: did not work- terminating");
        System.exit(1);
      }
      if (noSetup) {
        System.exit(0);
      }
      Settings.runningSetupInValidContext = true;
      Settings.runningSetupInContext = workDir;
      Settings.runningSetupWithJar = localJar;
      logToFile = false;
    }

//**API** sikulixapi.jar should not be runnable without defined options
    if (!Settings.runningSetupInValidContext && runningJar.contains("sikulixapi")) {
      System.exit(0);
    }

		if (logToFile) {
			logfile = (new File(workDir, localLogfile)).getAbsolutePath();
			if (!Debug.setLogFile(logfile)) {
				popError(workDir + "\n... folder we are running in must be user writeable! \n"
								+ "please correct the problem and start again.");
				System.exit(0);
			}
		}

		if (args.length > 0) {
			log1(lvl, "... starting with: " + Sikulix.arrayToString(args));
		} else {
			log1(lvl, "... starting with no args given");
		}

    if (logToFile) {
      Settings.getStatus(lvl);
    }
//</editor-fold>

    log1(lvl, "Setup in: %s using: %s", workDir, (runningJar.contains("classes") ? "Development Project" : runningJar));
		log1(lvl, "SikuliX Setup Build: %s %s", Settings.getVersionShort(), Settings.SikuliVersionBuild);

		File localJarIDE = new File(workDir, localIDE);
		File localJarJava = new File(workDir, localJava);
		File localMacFolder = new File(workDir, folderMacApp);

    //TODO Windows 8 HKLM/SOFTWARE/JavaSoft add Prefs ????

		//<editor-fold defaultstate="collapsed" desc="checking update/beta">
		if (!runningUpdate && !isUpdateSetup) {
			String uVersion = "";
			String msgFooter = "You have " + Settings.getVersion()
							+ "\nClick YES, if you want to install ..."
							+ "\ncurrent stuff will be saved to BackUp."
							+ "\n... Click NO to skip ...";
			if (localJarIDE.exists() || localJarJava.exists() || localMacFolder.exists()) {
				int avail = -1;
				boolean someUpdate = false;
				String ask1 = "You have " + Settings.getVersion()
								+ "\nClick YES if you want to run setup again\n"
								+ "This will download fresh versions of the selected stuff.\n"
								+ "Your current stuff will be saved to folder BackUp.\n\n"
								+ "If you cancel the setup later or it is not successful\n"
								+ "the saved stuff will be restored from folder BackUp\n\n";
				if (!popAsk(ask1)) {
					userTerminated("Do not run setup again");
				}
				//<editor-fold defaultstate="collapsed" desc="update - currently deactivated">
//				String ask2 = "Click YES to get info on updates or betas.\n"
//								+ "or click NO to terminate setup now.";
//				if (generallyDoUpdate && popAsk(ask2)) {
//					splash = showSplash("Checking for update or beta versions! (you have " + version + ")",
//									"please wait - may take some seconds ...");
//					AutoUpdater au = new AutoUpdater();
//					avail = au.checkUpdate();
//					closeSplash(splash);
//					if (avail > 0) {
//						if (avail == AutoUpdater.BETA || avail == AutoUpdater.SOMEBETA) {
//							someUpdate = true;
//							uVersion = au.getBetaVersion();
//							if (popAsk("Version " + uVersion + " is available\n" + msgFooter)) {
//								isBeta = true;
//							}
//						}
//						if (avail > AutoUpdater.FINAL) {
//							avail -= AutoUpdater.SOMEBETA;
//						}
//						if (avail > 0 && avail != AutoUpdater.BETA) {
//							someUpdate = true;
//							if (popAsk(au.whatUpdate + "\n" + msgFooter)) {
//								isUpdate = true;
//								uVersion = au.getVersionNumber();
//							}
//						}
//					}
//					if (!someUpdate) {
//						popInfo("No suitable update or beta available");
//						userTerminated("No suitable update or beta available");
//					}
//				}
				//</editor-fold>
				if (!isBeta && !isUpdate) {
					reset(-1);
				} else {
					//<editor-fold defaultstate="collapsed" desc="update - currently deactivated">
					log0(lvl, "%s is available", uVersion);
					if (uVersion.equals(updateVersion)) {
						reset(avail);
						Settings.downloadBaseDir = Settings.downloadBaseDirBase + uVersion.substring(0, 3) + "/";
						downloadSetup = "sikuli-update-" + uVersion + ".jar";
						if (!download(Settings.downloadBaseDir, workDir, downloadSetup,
										new File(workDir, downloadSetup).getAbsolutePath(), "")) {
							restore(true);
							popError("Download did not complete successfully.\n"
											+ "Check the logfile for possible error causes.\n\n"
											+ "If you think, setup's inline download from Dropbox is blocked somehow on,\n"
											+ "your system, you might download manually (see respective FAQ)\n"
											+ "For other reasons, you might simply try to run setup again.");
							terminate("download not completed successfully");
						}
						popInfo("Now you can run the update process:\n"
										+ "DoubleClick " + "sikuli-update-" + uVersion + ".jar"
										+ "\nin folder " + workDir + "\n\nPlease click OK before proceeding!");
						terminate("");
					} else {
						popError("downloadable update: " + uVersion + "\nexpected update: " + updateVersion
										+ "\n do not match --- terminating --- pls. report");
						terminate("update versions do not match");
					}
					//</editor-fold>
				}
			}
		} else {
			//<editor-fold defaultstate="collapsed" desc="update - currently deactivated">
			log0(lvl, "Update started");
			if (!generallyDoUpdate) {
				terminate("Switched Off: Run update!");
			}
			if (!popAsk("You requested to run an Update now"
							+ "\nYES to continue\nNO to terminate")) {
				userTerminated("");
			}
			//</editor-fold>
		}
    //</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="dispatching external setup run - currently not possible (update)">
		if (!isUpdateSetup && !runningSetup) {
//      String[] cmd = null;
//      File fCmd = null;
//      String runSetupOption = "";
//      if (isRunningUpdate()) {
//        runSetupOption = "updateSetup";
//      } else if (!runningSetup) {
//        runSetupOption = "runningSetup";
//      }
//      if (Settings.isWindows()) {
//        log0(lvl, "Extracting runSetup.cmd");
//        String syspath = System.getenv("PATH");
//        for (String p : syspath.split(";")) {
//          log0(lvl, "syspath: " + p);
//        }
//        loader.export("Commands/windows#runSetup.cmd", workDir);
//        fCmd = new File(workDir, "runSetup.cmd");
//        cmd = new String[]{"cmd", "/C", "start", "cmd", "/K", fCmd.getAbsolutePath(), runSetupOption};
//      } else if (runningUpdate) {
//        log0(lvl, "Extracting runSetup");
//        fCmd = new File(workDir, "runSetup");
//        loader.export("Commands/"
//                + (Settings.isMac() ? "mac#runSetup" : "linux#runSetup"), workDir);
//        loader.doSomethingSpecial("runcmd", new String[]{"chmod", "ugo+x", fCmd.getAbsolutePath()});
//        if (Settings.isMac()) {
//          cmd = new String[]{"/bin/sh", fCmd.getAbsolutePath(), runSetupOption};
//        } else {
//          cmd = new String[]{"/bin/bash", fCmd.getAbsolutePath(), runSetupOption};
//        }
//      }
//      if ((Settings.isWindows() || runningUpdate) && (fCmd == null || !fCmd.exists())) {
//        String msg = "Fatal error 002: runSetup(.cmd) could not be exported to " + workDir;
//        log0(-1, msg);
//        popError(msg);
//        System.exit(2);
//      }
//      if (runningUpdate) {
//        localSetup = "sikuli-setup-" + updateVersion.substring(0, 3) + ".jar";
//        FileManager.deleteFileOrFolder(new File(workDir, localSetup).getAbsolutePath());
//        log0(lvl, "Update: trying to dowload the new sikuli-setup.jar version " + updateVersion.substring(0, 3));
//        downloadSetup = "sikuli-setup-" + updateVersion + ".jar";
//        downloadBaseDir = downloadBaseDirBase + updateVersion.substring(0, 3) + "/";
//        if (!download(downloadBaseDir, workDir, downloadSetup,
//                new File(workDir, localSetup).getAbsolutePath())) {
//          restore();
//          popError("Download did not complete successfully.\n"
//                  + "Check the logfile for possible error causes.\n\n"
//                  + "If you think, setup's inline download from Dropbox is blocked somehow on,\n"
//                  + "your system, you might download manually (see respective FAQ)\n"
//                  + "For other reasons, you might simply try to run setup again.");
//          terminate("download not completed successfully");
//        }
//      }
//      if (cmd != null) {
//        if (runningUpdate && !popAsk("Continue Update after download success?"
//                + "\nYES to continue\nNO to terminate")) {
//          userTerminated("after download success");
//        }
//        log0(lvl, "dispatching external setup run");
//        if (runningfromJar) {
//          loader.doSomethingSpecial("runcmd", cmd);
//          System.exit(0);
//        }
//      }
		}
    //</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="option setup preps display options">
    String proxyMsg = "";
    if (!test) {
      getIDE = false;
      getJython = false;
      getJava = false;
    }
    if (!test) {
      if (!isUpdateSetup) {
        popInfo("Please read carefully before proceeding!!");
        winSetup = new JFrame("SikuliX-Setup");
        Border rpb = new LineBorder(Color.YELLOW, 8);
        winSetup.getRootPane().setBorder(rpb);
        Container winCP = winSetup.getContentPane();
        winCP.setLayout(new BorderLayout());
        winSU = new SetUpSelect();
        winCP.add(winSU, BorderLayout.CENTER);
        winSU.option2.setSelected(true);
        winSetup.pack();
        winSetup.setLocationRelativeTo(null);
        winSetup.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        winSetup.setVisible(true);

        //setup version basic
        winSU.suVersion.setText(Settings.getVersionShort() + "   (" + Settings.SikuliVersionBuild + ")");

        // running system
        Settings.getOS();
        msg = Settings.osName + " " + Settings.getOSVersion();
        winSU.suSystem.setText(msg);
        log1(lvl, "RunningSystem: " + msg);

        // folder running in
        winSU.suFolder.setText(workDir);
        log1(lvl, "parent of jar/classes: %s", workDir);

        // running Java
        String osarch = System.getProperty("os.arch");
        msg = "Java " + Settings.JavaVersion + " (" + osarch + ") " + Settings.JREVersion;
        winSU.suJava.setText(msg);
        log1(lvl, "RunningJava: " + msg);

        String pName = prefs.get("ProxyName", "");
        String pPort = prefs.get("ProxyPort", "");
        if (!pName.isEmpty() && !pPort.isEmpty()) {
          prefsHaveProxy = true;
          winSU.pName.setText(pName);
          winSU.pPort.setText(pPort);
        }

        winSU.addPropertyChangeListener("background", new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent pce) {
            winSetup.setVisible(false);
          }
        });

        while (true) {
          if (winSU.getBackground() == Color.YELLOW) {
            break;
          }
          try {
            Thread.sleep(1000);
          } catch (InterruptedException ex) {
          }
        }

        pName = winSU.pName.getText();
        pPort = winSU.pPort.getText();
        if (!pName.isEmpty() && !pPort.isEmpty()) {
          if (FileManager.setProxy(pName, pPort)) {
            log1(lvl, "Requested to run with proxy: %s ", Settings.proxy);
            proxyMsg = "... using proxy: " + Settings.proxy;
          }
        } else if (prefsHaveProxy) {
          prefs.put("ProxyName", "");
          prefs.put("ProxyPort", "");
        }
        Settings.proxyChecked = true;
      }

      File fPrefs = new File(workDir, "SikuliPrefs.txt");
      prefs.exportPrefs(fPrefs.getAbsolutePath());
      BufferedReader pInp = null;
      try {
        pInp = new BufferedReader(new FileReader(fPrefs));
        String line;
        while (null != (line = pInp.readLine())) {
          if (!line.contains("entry")) {
            continue;
          }
          if (logToFile) {
            log(lvl, "Prefs: " + line.trim());
          }
        }
        pInp.close();
      } catch (Exception ex) {
      }
      FileManager.deleteFileOrFolder(fPrefs.getAbsolutePath());

      if (!isUpdateSetup) {
        if (winSU.option1.isSelected()) {
          getIDE = true;
          if (winSU.option2.isSelected()) {
            getJython = true;
          }
          if (winSU.option3.isSelected()) {
            getJRuby = true;
            if (winSU.option8.isSelected()) {
              getJRubyAddOns = false;
            }
          }
          if (!getJython && !getJRuby) {
            getIDE = false;
          }
        }
        if (winSU.option4.isSelected()) {
          getJava = true;
        }
        if (winSU.option5.isSelected()) {
          if (Settings.isLinux()) {
            popInfo("You selected option 3 (Tesseract support)\n"
                    + "On Linux this does not make sense, since it\n"
                    + "is your responsibility to setup Tesseract on your own.\n"
                    + "This option will be ignored.");
          } else {
            getTess = true;
          }
        }
        if (winSU.option6.isSelected()) {
          forAllSystems = true;
        }
        if (winSU.option7.isSelected()) {
          getRServer = true;
        }

        if (((getTess || forAllSystems) && !(getIDE || getJava))) {
          popError("You only selected Option 3 or 4 !\n"
                  + "This is currently not supported.\n"
                  + "Please start allover again with valid options.\n");
          terminate("");
        }
        msg = "The following file(s) will be downloaded to\n"
                + workDir + "\n";
      } else {
        msg = "The following packages will be updated\n";
        if (Settings.proxy != null) {
          msg += "... using proxy: " + Settings.proxy + "\n";
        }
        if (new File(workDir, localIDE).exists()) {
          getIDE = true;
          msg += "Pack 1: " + localIDE + "\n";
        }
        if (new File(workDir, localJava).exists()) {
          getJava = true;
          msg += "Pack 2: " + localJava + "\n";
        }
        if (new File(workDir, localRServer).exists()) {
          getRServer = true;
          msg += localRServer + "\n";
        }
        if (new File(workDir, localTess).exists()) {
          getTess = true;
          msg += "\n... with Tesseract OCR support\n\n";
        }
        if (popAsk("It cannot be detected, wether your current jars\n"
                + "have been setup for all systems (option 4).\n"
                + "Click YES if you want this option now\n"
                + "Click NO to run normal setup for current system")) {
          forAllSystems = true;
        }
      }
    }

		downloadedFiles = "";
		if (!isUpdateSetup) {
			if (getIDE || getJava || getRServer) {

				if (!proxyMsg.isEmpty()) {
					msg += proxyMsg + "\n";
				}
				if (getIDE) {
					downloadedFiles += downloadIDE + " ";
					msg += "\n--- Package 1 ---\n" + downloadIDE + " (IDE/Scripting)";
					if (getJython) {
						downloadedFiles += downloadJython + " ";
						msg += "\n - with Jython";
					}
					if (getJRuby) {
						downloadedFiles += downloadJRuby + " ";
              msg += "\n - with JRuby";
            if (getJRubyAddOns) {
              downloadedFiles += downloadJRubyAddOns + " ";
              msg += " incl. AddOns";
            }
					}
//					if (Settings.isMac()) {
//            downloadedFiles += downloadMacApp + " ";
//						msg += "\n" + downloadMacApp + " (Mac-App)";
//					}
				}
				if (getTess || getRServer) {
					if (getIDE || getJava) {
						msg += "\n";
					}
					msg += "\n--- Additions ---";
					if (getTess) {
						downloadedFiles += downloadTess + " ";
						msg += "\n" + downloadTess + " (Tesseract)";
					}
					if (getRServer) {
						downloadedFiles += downloadRServer + " ";
						msg += "\n" + downloadRServer + " (RemoteServer)";
					}
				}
			}
		}

		if (getIDE || getJava || getRServer) {
			if (getIDE || getRServer) {
				msg += "\n\nOnly click NO, if you want to terminate setup now!\n"
								+ "Click YES even if you want to use local copies in Downloads!";
				if (!popAsk(msg)) {
					terminate("");
				}
			}
		} else {
			popError("Nothing selected! You might try again ;-)");
			terminate("");
		}

		// downloading
		localJar = null;
		String targetJar;
		boolean downloadOK = true;
		boolean dlOK = true;
		File fDLDir = new File(workDir, "Downloads");
		fDLDir.mkdirs();
		String dlDir = fDLDir.getAbsolutePath();
		if (getIDE) {
			localJar = new File(workDir, localIDE).getAbsolutePath();
      dlOK = download(Settings.downloadBaseDir, dlDir, downloadIDE, localJar, "IDE/Scripting");
			downloadOK &= dlOK;
//			if (Settings.isMac()) {
//				targetJar = new File(workDir, localMacApp).getAbsolutePath();
//				if (!test) {
//					dlOK = download(downloadBaseDir, dlDir, downloadMacApp, targetJar, "MacApp");
//				}
//				if (dlOK) {
//					FileManager.deleteFileOrFolder((new File(workDir, folderMacApp)).getAbsolutePath());
//					FileManager.unpackJar(targetJar, workDir, false);
//					FileManager.deleteFileOrFolder(new File(workDir, "META-INF").getAbsolutePath());
//				}
//				downloadOK &= dlOK;
//			}
		}
		if (getJython) {
			targetJar = new File(workDir, localJython).getAbsolutePath();
      downloadOK = download(Settings.downloadBaseDir, dlDir, downloadJython, targetJar, "Jython");
			downloadOK &= dlOK;
		}
		if (getJRuby) {
			targetJar = new File(workDir, localJRuby).getAbsolutePath();
      downloadOK = download(Settings.downloadBaseDir, dlDir, downloadJRuby, targetJar, "JRuby");
			downloadOK &= dlOK;
			if (downloadOK && getJRubyAddOns) {
				targetJar = new File(workDir, localJRubyAddOns).getAbsolutePath();
        downloadOK = download(Settings.downloadBaseDir, dlDir, downloadJRubyAddOns, targetJar, "JRubyAddOns");
				downloadOK &= dlOK;
			}
		}
		if (getTess) {
			targetJar = new File(workDir, localTess).getAbsolutePath();
      downloadOK = download(Settings.downloadBaseDir, dlDir, downloadTess, targetJar, "Tesseract");
			downloadOK &= dlOK;
		}
		if (getRServer) {
			targetJar = new File(workDir, localRServer).getAbsolutePath();
      downloadOK = download(Settings.downloadBaseDir, dlDir, downloadRServer, targetJar, "RemoteServer");
			downloadOK &= dlOK;
		}
    if (!downloadedFiles.isEmpty()) {
      log1(lvl, "Download ended");
      log1(lvl, "Downloads for selected options:\n" + downloadedFiles);
      log1(lvl, "Download page: " + Settings.downloadBaseDirWeb);
    }
		if (!downloadOK) {
			popError("Some of the downloads did not complete successfully.\n"
							+ "Check the logfile for possible error causes.\n\n"
							+ "If you think, setup's inline download is blocked somehow on,\n"
							+ "your system, you might download the appropriate raw packages manually\n"
							+ "into the folder Downloads in the setup folder and run setup again.\n\n"
							+ "download page: " + Settings.downloadBaseDirWeb + "\n"
							+ "files to download (information is in the setup log file too)\n"
							+ downloadedFiles
							+ "\n\nBe aware: The raw packages are not useable without being processed by setup!\n\n"
							+ "For other reasons, you might simply try to run setup again.");
			terminate("download not completed successfully");
		}
    //</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="option setup: add needed stuff">
		if (!getIDE && !getJava) {
			log1(lvl, "Nothing else to do");
			System.exit(0);
		}

		if (Settings.isLinux()) {
			if (popAsk("If you have provided your own builds\n"
							+ "of the native libraries in the system paths:\n"
							+ "Click YES if you did (be sure, they are there)\n"
							+ "Click NO to pack the bundled libs to the jars.")) {
				shouldPackLibs = false;
			}
      if (test) {
        shouldPackLibs = true;
      }
		}

		boolean success = true;
		FileManager.JarFileFilter libsFilter = new FileManager.JarFileFilter() {
			@Override
			public boolean accept(ZipEntry entry) {
				if (forAllSystems) {
					if (!shouldPackLibs && entry.getName().startsWith("META-INF/libs/linux")
									&& entry.getName().contains("VisionProxy")) {
						return false;
					}
					return true;
				} else if (Settings.isWindows()) {
					if (entry.getName().startsWith("META-INF/libs/mac")
									|| entry.getName().startsWith("META-INF/libs/linux")
									|| entry.getName().startsWith("jxgrabkey")) {
						return false;
					}
				} else if (Settings.isMac()) {
					if (entry.getName().startsWith("META-INF/libs/windows")
									|| entry.getName().startsWith("META-INF/libs/linux")
									|| entry.getName().startsWith("com.melloware.jintellitype")
									|| entry.getName().startsWith("jxgrabkey")) {
						return false;
					}
				} else if (Settings.isLinux()) {
					if (entry.getName().startsWith("META-INF/libs/windows")
									|| entry.getName().startsWith("META-INF/libs/mac")
									|| entry.getName().startsWith("com.melloware.jintellitype")) {
						return false;
					}
					if (!shouldPackLibs && entry.getName().contains("VisionProxy")) {
						return false;
					}
				}
				return true;
			}
		};

		String[] jarsList = new String[]{null, null, null, null, null, null};
		String localTemp = "sikulixtemp.jar";
		splash = showSplash("Now adding needed stuff to selected jars.", "please wait - may take some seconds ...");

		jarsList[1] = (new File(workDir, localSetup)).getAbsolutePath();
		if (getTess) {
			jarsList[2] = (new File(workDir, localTess)).getAbsolutePath();
		}

		if (success && getJava) {
			log1(lvl, "adding needed stuff to sikulixapi.jar");
			localJar = (new File(workDir, localJava)).getAbsolutePath();
			targetJar = (new File(workDir, localTemp)).getAbsolutePath();
			success &= FileManager.buildJar(targetJar, jarsList, null, null, libsFilter);
			success &= handleTempAfter(localTemp, localJar);
		}

		if (success && getIDE) {
			log1(lvl, "adding needed stuff to sikulix.jar");
			localJar = (new File(workDir, localIDE)).getAbsolutePath();
			jarsList[0] = localJar;
			if (getJython) {
				jarsList[3] = (new File(workDir, localJython)).getAbsolutePath();
			}
			if (getJRuby) {
				jarsList[4] = (new File(workDir, localJRuby)).getAbsolutePath();
				if (getJRubyAddOns) {
          jarsList[5] = (new File(workDir, localJRubyAddOns)).getAbsolutePath();
        }
			}
			targetJar = (new File(workDir, localTemp)).getAbsolutePath();
			success &= FileManager.buildJar(targetJar, jarsList, null, null, libsFilter);
			success &= handleTempAfter(localTemp, localJar);
		}

		if (getJython) {
			new File(workDir, localJython).delete();
		}
		if (getJRuby) {
			new File(workDir, localJRuby).delete();
			if (getJRubyAddOns) {
        new File(workDir, localJRubyAddOns).delete();
      }
		}
		if (getTess) {
			new File(workDir, localTess).delete();
		}

//		if (Settings.isMac() && getIDE && success) {
//			closeSplash(splash);
//			log0(lvl, "preparing Mac app as SikulixUtil-IDE.app");
//			splash = showSplash("Now preparing Mac app SikulixUtil-IDE.app.", "please wait - may take some seconds ...");
//			forAllSystems = false;
//			targetJar = (new File(workDir, localMacAppIDE)).getAbsolutePath();
//			jarsList = new String[]{(new File(workDir, localIDE)).getAbsolutePath()};
//			success &= FileManager.buildJar(targetJar, jarsList, null, null, libsFilter);
//		}
		closeSplash(splash);

		ResourceLoader loader = ResourceLoader.get();
		if (success && (getIDE)) {
			log1(lvl, "exporting commandfiles");
			splash = showSplash("Now exporting commandfiles.", "please wait - may take some seconds ...");

			if (Settings.isWindows()) {
				if (getIDE) {
					loader.export("Commands/windows#" + runsikulix + ".cmd", workDir);
				}

			} else if (Settings.isMac()) {
				if (getIDE) {
					String fmac = new File(workDir, folderMacAppContent).getAbsolutePath();
//					loader.export("Commands/mac#" + runsikulix, fmac);
					loader.export("Commands/mac#" + runsikulix, workDir);
//					loader.doSomethingSpecial("runcmd", new String[]{"chmod", "ugo+x",
//						new File(fmac, runsikulix).getAbsolutePath()});
//					loader.doSomethingSpecial("runcmd", new String[]{"chmod", "ugo+x",
//						new File(fmac, "MacOS/droplet").getAbsolutePath()});
					ResourceLoader.get().runcmd(new String[]{"chmod", "ugo+x", new File(workDir, runsikulix).getAbsolutePath()});
//					FileManager.deleteFileOrFolder(new File(workDir, localMacApp).getAbsolutePath());
				}
			} else if (Settings.isLinux()) {
				if (getIDE) {
					loader.export("Commands/linux#" + runsikulix, workDir);
					ResourceLoader.get().runcmd(new String[]{"chmod", "ugo+x", new File(workDir, runsikulix).getAbsolutePath()});
					ResourceLoader.get().runcmd(new String[]{"chmod", "ugo+x", new File(workDir, localIDE).getAbsolutePath()});
				}
			}
			closeSplash(splash);
		}
		if (!success) {
			popError("Bad things happened trying to add native stuff to selected jars --- terminating!");
			terminate("Adding stuff to jars did not work");
		}
		restore(true); //to get back the stuff that was not changed
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="option setup: environment setup and test">
		log1(lvl, "trying to set up the environment");
		splash = showSplash("Now trying to set up Sikuli environment!", "please wait - may take some seconds ...");
		File folderLibs = new File(workDir, "libs");

		if (folderLibs.exists()) {
			FileManager.deleteFileOrFolder(folderLibs.getAbsolutePath());
		}
		folderLibs.mkdirs();

		if (loader.check(Settings.SIKULI_LIB)) {
			closeSplash(splash);
			splash = showSplash(" ", "Environment seems to be ready!");
			closeSplash(splash);
		} else {
			closeSplash(splash);
			popError("Something serious happened! Sikuli not useable!\n"
							+ "Check the error log at " + (logfile == null ? "printout" : logfile));
			terminate("Setting up environment did not work");
		}

    URL uTess = null;
    if (getJava) {
			log1(lvl, "Trying to run functional test: JAVA-API");
			splash = showSplash("Trying to run functional test(s)", "Java-API: org.sikuli.script.Sikulix.testSetup()");
			if (!Sikulix.addToClasspath(localJarJava.getAbsolutePath())) {
				closeSplash(splash);
				log0(-1, "Java-API test: ");
				popError("Something serious happened! Sikuli not useable!\n"
								+ "Check the error log at " + (logfile == null ? "printout" : logfile));
				terminate("Functional test JAVA-API did not work", 1);
			}
			try {
				log0(lvl, "trying to run org.sikuli.script.Sikulix.testSetup()");
				loader.setItIsJython(); // export Lib folder
				if (getTess) {
          try {
            uTess = (new URI("file", localJarJava.getAbsolutePath(), null)).toURL();
          } catch (Exception ex){ }
          ResourceLoader.get().exportTessdata(uTess);
          getTess = false;
				}
				Class sysclass = URLClassLoader.class;
				Class SikuliCL = sysclass.forName("org.sikuli.script.Sikulix");
				log0(lvl, "class found: " + SikuliCL.toString());
        Method method = null;
        if (test) {
          method = SikuliCL.getDeclaredMethod("testSetupSilent", new Class[0]);
        } else {
          method = SikuliCL.getDeclaredMethod("testSetup", new Class[0]);
        }
				log0(lvl, "getMethod: " + method.toString());
				method.setAccessible(true);
				closeSplash(splash);
				log0(lvl, "invoke: " + method.toString());
				Object ret = method.invoke(null, new Object[0]);
				if (!(Boolean) ret) {
					throw new Exception("testSetup returned false");
				}
			} catch (Exception ex) {
				closeSplash(splash);
				log0(-1, ex.getMessage());
				popError("Something serious happened! Sikuli not useable!\n"
								+ "Check the error log at " + (logfile == null ? "printout" : logfile));
				terminate("Functional test Java-API did not work", 1);
			}
		}
		if (getIDE) {
			if (!Sikulix.addToClasspath(localJarIDE.getAbsolutePath())) {
				closeSplash(splash);
				popError("Something serious happened! Sikuli not useable!\n"
								+ "Check the error log at " + (logfile == null ? "printout" : logfile));
				terminate("Functional test IDE did not work", 1);
			}
      if (getTess) {
        try {
          uTess = (new URI("file", localJarIDE.getAbsolutePath(), null)).toURL();
        } catch (Exception ex){ }
        ResourceLoader.get().exportTessdata(uTess);
      }
      String testMethod;
			if (getJython) {
				if (test) {
					testMethod = "print \"testSetup: Jython: success\"";
				} else {
					testMethod = "Sikulix.testSetupJython()";
				}
				log1(lvl, "Jython: Trying to run functional test: running script statements via SikuliScript");
        splash = showSplash("Jython Scripting: Trying to run functional test",
                "Running script statements via SikuliScript");
				try {
					String testargs[] = new String[]{"-testSetup", "jython", testMethod};
					closeSplash(splash);
					runScriptTest(testargs);
					if (null == testargs[0]) {
						throw new Exception("testSetup ran with problems");
					}
				} catch (Exception ex) {
					closeSplash(splash);
					log0(-1, ex.getMessage());
					popError("Something serious happened! Sikuli not useable!\n"
									+ "Check the error log at " + (logfile == null ? "printout" : logfile));
					terminate("Functional test Jython did not work", 1);
				}
			}
			if (getJRuby) {
				if (test) {
					testMethod = "print \"testSetup: JRuby: success\"";
				} else {
					testMethod = "Sikulix.testSetupJRuby()";
				}
        log1(lvl, "JRuby: Trying to run functional test: running script statements via SikuliScript");
        splash = showSplash("JRuby Scripting: Trying to run functional test",
                "Running script statements via SikuliScript");
				try {
					String testargs[] = new String[]{"-testSetup", "jruby", testMethod};
					closeSplash(splash);
					runScriptTest(testargs);
					if (null == testargs[0]) {
						throw new Exception("testSetup ran with problems");
					}
				} catch (Exception ex) {
					closeSplash(splash);
					log0(-1, "content of returned error's (%s) message:\n%s", ex, ex.getMessage());
					popError("Something serious happened! Sikuli not useable!\n"
									+ "Check the error log at " + (logfile == null ? "printout" : logfile));
					terminate("Functional test JRuby did not work", 1);
				}
			}
		}

		splash = showSplash("Setup seems to have ended successfully!",
            "Detailed information see: " + (logfile == null ? "printout" : logfile));
		start += 2000;

		closeSplash(splash);

		log0(lvl,
						"... SikuliX Setup seems to have ended successfully ;-)");
		//</editor-fold>

		System.exit(0);
	}

	private static void runScriptTest(String[] testargs) {

	}

  private static boolean createSetupFolder(String path) {
    String projectDir = null, targetDir = null;
    boolean success = true;
    boolean doit = true;
    File setupjar = null;
    if (path.isEmpty()) {
      if ("classes".equals(runningJar) || runningJar.endsWith("-plain.jar")) {
        projectDir = new File(workDir).getParentFile().getParentFile().getAbsolutePath();
      } else {
        success = false;
      }
      if (success) {
        setupjar = new File(new File(projectDir, "Setup/target"),
                localSetup.replace(".jar", "-plain.jar"));
        if (!setupjar.exists()) {
          success = false;
        }
      }
      if (success) {
        if (new File(projectDir, "Setup").exists()) {
          File ftargetDir = new File(projectDir, "Setup/target/Setup");
					if (ftargetDir.exists()) {
						FileManager.deleteFileOrFolder(ftargetDir.getAbsolutePath());
					}
          ftargetDir.mkdirs();
          targetDir = ftargetDir.getAbsolutePath();
        } else {
          success = false;
        }
      }
      if (!success) {
        log(-1, "createSetupFolder: Setup folder or %s missing", setupjar.getAbsolutePath());
        return false;
      }

      File jythonJar = new File(Settings.SikuliJython);
      File jrubyJar = new File(Settings.SikuliJRuby);
      String ideFat = "sikulix-complete-" + Settings.SikuliProjectVersion + "-ide-fat.jar";
      File fIDEFat = new File(projectDir, "IDEFat/target/" + ideFat);
      if (!fIDEFat.exists()) {
        log(-1, "createSetupFolder: missing: " + fIDEFat.getAbsolutePath());
        success = false;
      }
      if (!jythonJar.exists()) {
        Debug.log(3, "createSetupFolder: missing: " + jythonJar.getAbsolutePath());
        success = false;
      }
      if (!jrubyJar.exists()) {
        Debug.log(3, "createSetupFolder: missing " + jrubyJar.getAbsolutePath());
        success = false;
      }
      String jrubyAddons = "sikulixjrubyaddons-" + Settings.SikuliProjectVersion + "-plain.jar";
      File fJRubyAddOns = new File(projectDir, "JRubyAddOns/target/" + jrubyAddons);
      if (!fJRubyAddOns.exists()) {
        Debug.log(3, "createSetupFolder: missing: " + fJRubyAddOns.getAbsolutePath());
        success = false;
      }
      if (success) {
        File fDownloads = new File(targetDir, "Downloads");
        if (new File(targetDir, "Downloads").exists()) {
          FileManager.deleteFileOrFolder(new File(targetDir, "Downloads").getAbsolutePath(), null);
        }
        fDownloads.mkdir();
        String fname = null;
        try {
          fname = setupjar.getAbsolutePath();
          FileManager.xcopy(fname,
                  new File(targetDir, localSetup).getAbsolutePath());
          fname = fIDEFat.getAbsolutePath();
          FileManager.xcopy(fname,
                  new File(fDownloads, downloadIDE).getAbsolutePath());
          fname = jythonJar.getAbsolutePath();
          FileManager.xcopy(fname,
                  new File(fDownloads, downloadJython).getAbsolutePath());
          fname = jrubyJar.getAbsolutePath();
          FileManager.xcopy(fname,
                  new File(fDownloads, downloadJRuby).getAbsolutePath());
          fname = fJRubyAddOns.getAbsolutePath();
          FileManager.xcopy(fname,
                  new File(fDownloads, downloadJRubyAddOns).getAbsolutePath());
          fname = new File(projectDir, "Remote/target/"
                  + "sikulixremote-" + Settings.SikuliProjectVersion + ".jar").getAbsolutePath();
          FileManager.xcopy(fname, new File(fDownloads, downloadRServer).getAbsolutePath());
          fname = new File(projectDir, "Tesseract/target/"
                  + Settings.SikuliProjectVersion + downloadTessSuffix).getAbsolutePath();
          FileManager.xcopy(fname, new File(fDownloads, downloadTess).getAbsolutePath());
          fname = new File(projectDir, "MacApp/target/"
                  + Settings.SikuliProjectVersion + downloadMacAppSuffix).getAbsolutePath();
          FileManager.xcopy(fname, new File(fDownloads, downloadMacApp).getAbsolutePath());
        } catch (Exception ex) {
          log(-1, "createSetupFolder: copying files did not work: %s", fname);
          success = false;
        }
      }
      if (success) {
        workDir = targetDir;
      }
    }
    return success;
  }

	private static boolean handleTempAfter(String temp, String target) {
		boolean success = true;
		log1(lvl, "trying to remove temp files");
		FileManager.deleteFileOrFolder(target);
		success &= !new File(target).exists();
		if (success) {
			success &= (new File(workDir, temp)).renameTo(new File(target));
			if (!success) {
				log1(-1, "rename temp to " + target + " --- trying copy");
				try {
					FileManager.xcopy(new File(workDir, temp).getAbsolutePath(), target);
					success = new File(target).exists();
					if (success) {
						FileManager.deleteFileOrFolder(new File(workDir, temp).getAbsolutePath());
						success = !new File(workDir, temp).exists();
					}
				} catch (IOException ex) {
					success &= false;
				}
				if (!success) {
					log1(-1, "did not work");
					terminate("");
				}
			}
		}
		return success;
	}

	private static boolean isRunningUpdate() {
		return runningUpdate;
	}

	private static boolean getProxy(String pn, String pp) {
		if (!pn.isEmpty()) {
			Pattern p = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
			if (p.matcher(pn).matches()) {
				Settings.proxyIP = pn;
			} else {
				Settings.proxyName = pn;
			}
			String msgp = String.format("Requested to use this Proxy: %s (%s)", pn, pp);
			log1(lvl, msgp);
			if (pp.isEmpty()) {
				popError(String.format("Proxy specification invalid: %s (%s)", pn, pp));
				log1(-1, "Terminating --- Proxy invalid");
				return false;
			} else {
				if (!popAsk(msgp)) {
					log1(-1, "Terminating --- User did not accept Proxy: %s %s", pn, pp);
					return false;
				}
			}
			Settings.proxyPort = pp;
			return true;
		}
		return false;
	}

	protected static void restore(boolean regular) {
    if (!regular) {
      log1(-1, "User requested termination");
    }
		if (!backUpExists) {
			return;
		}
		String backup = new File(workDir, "Backup").getAbsolutePath();
		if (new File(backup, localIDE).exists() && !new File(workDir, localIDE).exists()) {
			log0(lvl, "restoring from backup " + localIDE);
			new File(backup, localIDE).renameTo(new File(workDir, localIDE));
		}
		if (new File(backup, localJava).exists() && !new File(workDir, localJava).exists()) {
			log0(lvl, "restoring from backup " + localJava);
			new File(backup, localJava).renameTo(new File(workDir, localJava));
		}
		if (new File(backup, localTess).exists() && !new File(workDir, localTess).exists()) {
			log0(lvl, "restoring from backup " + localTess);
			new File(backup, localTess).renameTo(new File(workDir, localTess));
		}
		if (new File(backup, localRServer).exists() && !new File(workDir, localRServer).exists()) {
			log0(lvl, "restoring from backup " + localRServer);
			new File(backup, localRServer).renameTo(new File(workDir, localRServer));
		}
		String folder = "Lib";
		if (new File(backup, folder).exists() && !new File(workDir, folder).exists()) {
			log0(lvl, "restoring from backup " + "folder " + folder);
			new File(backup, folder).renameTo(new File(workDir, folder));
		}
		folder = "libs";
		if (new File(backup, folder).exists() && !new File(workDir, folder).exists()) {
			log0(lvl, "restoring from backup " + "folder " + folder);
			new File(backup, folder).renameTo(new File(workDir, folder));
		}
//    FileManager.deleteFileOrFolder(new File(workDir, "Backup").getAbsolutePath());
		FileManager.deleteFileOrFolder(new File(workDir, "SikuliPrefs.txt").getAbsolutePath());
	}

	private static void reset(int type) {
		log1(3, "requested to reset: " + workDir);
		String message = "";
		if (type <= 0) {
			message = "You decided to run setup again!\n";
		} else if (isBeta) {
			message = "You decided to install a beta version!\n";
		} else if (isUpdate) {
			message = "You decided to install a new version!\n";
		}
		File fBackup = new File(workDir, "BackUp");
		if (fBackup.exists()) {
			if (!popAsk(message + "A backup folder exists and will be purged!\n"
							+ "Click YES if you want to proceed.\n"
							+ "Click NO, to first save the current backup folder and come back. ")) {
				System.exit(0);
			}
		}
		splash = showSplash("Now creating backup and cleaning setup folder", "please wait - may take some seconds ...");
		String backup = fBackup.getAbsolutePath();
		FileManager.deleteFileOrFolder(backup, new FileManager.FileFilter() {
			@Override
			public boolean accept(File entry) {
				return true;
			}
		});
		try {
			FileManager.xcopy(workDir, backup);
		} catch (IOException ex) {
			popError("Reset: Not possible to backup:\n" + ex.getMessage());
			terminate("Reset: Not possible to backup:\n" + ex.getMessage());
		}
		FileManager.deleteFileOrFolder(workDir, new FileManager.FileFilter() {
			@Override
			public boolean accept(File entry) {
				if (entry.getName().startsWith("run")) {
					return false;
				} else if (entry.getName().equals(localSetup)) {
					return false;
				} else if (workDir.equals(entry.getAbsolutePath())) {
					return false;
				} else if ("BackUp".equals(entry.getName())) {
					return false;
				} else if ("Downloads".equals(entry.getName())) {
					return false;
				} else if (entry.getName().contains("SetupLog")) {
					return false;
				}
				return true;
			}
		});
		closeSplash(splash);
		log1(3, "backup completed!");
		backUpExists = true;
	}

	protected static void helpOption(int option) {
		String m;
		String om = "";
		m = "\n-------------------- Some Information on this option, that might "
						+ "help to decide, wether to select it ------------------";
		switch (option) {
			case (1):
				om = "Package 1: You get SikuliX (sikulix.jar) which supports all usages of Sikuli";
//              -------------------------------------------------------------
				m += "\nIt is recommended for people new to Sikuli to get a feeling about the features";
				m += "\n - and those who want to develop Sikuli scripts with the Sikuli IDE";
				m += "\n - and those who want to run Sikuli scripts from commandline.";
				m += "\nDirectly supported scripting languages are Jython and JRuby (you might choose one of them or even both)";
				m += "\n\nFor those who know ;-) additionally you can ...";
				m += "\n- develop Java programs with Sikuli features in IDE's like Eclipse, NetBeans, ...";
				m += "\n- develop in any Java aware scripting language adding Sikuli features in IDE's like Eclipse, NetBeans, ...";
				m += "\n\nSpecial INFO for Jython, JRuby and Java developement";
				m += "\nIf you want to use standalone Jython/JRuby or want to develop in Java in parallel,";
				m += "\nyou should select Package 2 additionally (Option 2)";
				m += "\nIn these cases, Package 1 (SikuliX) can be used for image management and for small tests/trials.";
				if (Settings.isWindows()) {
					m += "\n\nSpecial info for Windows systems:";
					m += "\nThe generated jars can be used out of the box with Java 32-Bit and Java 64-Bit as well.";
					m += "\nThe Java version is detected at runtime and the native support is switched accordingly.";
				}
//				if (Settings.isMac()) {
//					m += "\n\nSpecial info for Mac systems:";
//					m += "\nFinally you will have a Sikuli-IDE.app in the setup working folder.";
//					m += "\nTo use it, just move it into the Applications folder.";
//					m += "\nIf you need to run stuff from commandline or want to use Sikuli with Java,";
//					m += "\nyou have the following additionally in the setup folder:";
//					m += "\nrunIDE: the shellscript to run scripts and";
//					m += "\nsikulix.jar: for all other purposes than IDE and running scripts";
//					m += "\nMind the above special info about Jython, JRuby and Java developement too.";
//				}
				break;
			case (2):
				om = "Package 2: To support developement in Java or any Java aware scripting language. you get sikulixapi.jar."
								+ "\nYou might want Package 1 (SikuliX) additionally to use the IDE for managing the images or some trials.";
//              -------------------------------------------------------------
				m += "\nThe content of this package is stripped down to what is needed to develop in Java"
								+ " or any Java aware scripting language \n(no IDE, no bundled script run support for Jython/JRuby)";
				m += "\n\nHence this package is not runnable and must be in the class path to use it"
								+ " for developement or at runtime";
				m += "\n\nSpecial info for usage with Jython/JRuby: It contains the Sikuli Jython/JRuby API ..."
								+ "\n... and adds itself to Jython/JRuby path at runtime"
								+ "\n... and exports the Sikuli Jython/JRuby modules to the folder Libs at runtime"
								+ "\nthat helps to setup the auto-complete in IDE's like NetBeans, Eclipse ...";
				if (Settings.isWindows()) {
					m += "\n\nSpecial info for Windows systems:";
					m += "\nThe generated jars can be used out of the box with Java 32-Bit and Java 64-Bit as well.";
					m += "\nThe Java version is detected at runtime and the native support is switched accordingly.";
				}
				break;
			case (3):
				om = "To get the additional Tesseract stuff into your packages to use the OCR engine";
//              -------------------------------------------------------------
				m += "\nOnly makes sense for Windows and Mac,"
								+ "\nsince for Linux the complete install of Tesseract is your job.";
				m += "\nFeel free to add this to your packages, \n...but be aware of the restrictions, oddities "
								+ "and bugs with the current OCR and text search feature.";
				m += "\nIt adds more than 10 MB to your jars and the libs folder at runtime."
								+ "\nSo be sure, that you really want to use it!";
				m += "\n\nIt is NOT recommended for people new to Sikuli."
								+ "\nYou might add this feature later after having gathered some experiences with Sikuli";
				break;
			case (4):
				om = "To prepare the selected packages to run on all supported systems";
//              -------------------------------------------------------------
				m += "\nWith this option NOT selected, the setup process will only add the system specific"
								+ " native stuff \n(Windows: support for both Java 32-Bit and Java 64-Bit is added)";
				m += "\n\nSo as a convenience you might select this option to produce jars, that are"
								+ " useable out of the box on Windows, Mac and Linux.";
				m += "\nThis is possible now, since the usage of Sikuli does not need any system specific"
								+ " preparations any more. \nJust use the package (some restrictions on Linux though).";
				m += "\n\nSome scenarios for usages in different system environments:";
				m += "\n- download or use the jars from a central network place ";
				m += "\n- use the jars from a stick or similar mobile medium";
				m += "\n- deploying Sikuli apps to be used all over the place";
				break;
			case (5):
				om = "To try out the experimental remote feature";
//              -------------------------------------------------------------
				m += "\nYou might start the downloaded jar on any system, that is reachable "
								+ "\nby other systems in your network via TCP/IP (hostname or IP-address)."
								+ "\nusing: java -jar sikulix-remoteserver.jar"
								+ "\n\nThe server is started and listens on a port (default 50000) for incoming requests"
								+ "\nto use the mouse or keyboard or send back a screenshot."
								+ "\nOn the client side a Sikuli script has to initiate a remote screen with the "
								+ "\nrespective IP-address and port of a running server and on connection success"
								+ "\nthe remote system can be used like a local screen/mouse/keyboard."
								+ "\n\nCurrently all basic operations like find, click, type ... are supported,"
								+ "\nbut be aware, that the search ops are done on the local system based on "
								+ "\nscreenshots sent back from the remote system on request."
								+ "\n\nMore information: https://github.com/RaiMan/SikuliX-Remote";
				break;
		}
		popInfo("asking for option " + option + ": " + om + "\n" + m);
	}

  private static String packMessage(String msg) {
    msg = msg.replace("\n\n", "\n");
    msg = msg.replace("\n\n", "\n");
    if (msg.startsWith("\n")) {
      msg = msg.substring(1);
    }
    if (msg.endsWith("\n")) {
      msg = msg.substring(0, msg.length() - 1);
    }
    return "--------------------\n" + msg + "\n--------------------";
  }

	private static void popError(String msg) {
		log1(3, "\npopError: " + packMessage(msg));
		if (!test) {
      Sikulix.popError(msg, "SikuliX-Setup: having problems ...");
    }
	}

	private static void popInfo(String msg) {
    log1(3, "\npopInfo: " + packMessage(msg));
		if (!test) Sikulix.popup(msg, "SikuliX-Setup: info ...");
	}

	private static boolean popAsk(String msg) {
    log1(3, "\npopAsk: " + packMessage(msg));
    if (test) {
      return true;
    }
		return Sikulix.popAsk(msg, "SikuliX-Setup: question ...");
	}

	private static JFrame showSplash(String title, String msg) {
    if (test) {
      return null;
    }
		start = (new Date()).getTime();
		return new SplashFrame(new String[]{"splash", "# " + title, "#... " + msg});
	}

	private static void closeSplash(JFrame splash) {
    if (splash == null) {
      return;
    }
		long elapsed = (new Date()).getTime() - start;
		if (elapsed < 3000) {
			try {
				Thread.sleep(3000 - elapsed);
			} catch (InterruptedException ex) {
			}
		}
		splash.dispose();
	}

	private static boolean download(String sDir, String tDir, String item, String jar, String itemName) {
		boolean shouldDownload = true;
		File downloaded = new File(tDir, item);
		if (downloaded.exists()) {
			if (popAsk("In your Setup/Downloads folder you already have: " + itemName + "\n"
							+ downloaded.getAbsolutePath()
							+ "\nClick YES, if you want to use this for setup processing\n\n"
							+ "... or click NO, to download a fresh copy")) {
				shouldDownload = false;
			}
		}
		if (shouldDownload) {
			JFrame progress = new SplashFrame("download");
			String fname = FileManager.downloadURL(sDir + item, tDir, progress);
			progress.dispose();
			if (null == fname) {
				log1(-1, "Fatal error 001: not able to download: %s", item);
				return false;
			}
		}
		try {
			FileManager.xcopy(downloaded.getAbsolutePath(), jar);
		} catch (IOException ex) {
			terminate("Unable to copy from Downloads: "
							+ downloaded.getAbsolutePath() + "\n" + ex.getMessage());
		}
		log(lvl, "Copied from Downloads: " + item);
    if (!shouldDownload) {
      downloadedFiles = downloadedFiles.replace(item + " ", "");
    }
		return true;
	}

	private static void userTerminated(String msg) {
		if (!msg.isEmpty()) {
			log1(lvl, msg);
		}
		log1(lvl, "User requested termination.");
		System.exit(0);
	}

  private static void prepTerminate(String msg) {
		if (msg.isEmpty()) {
			restore(true);
		} else {
			log1(-1, msg);
			log1(-1, "... terminated abnormally :-(");
			popError("Something serious happened! Sikuli not useable!\n"
							+ "Check the error log at " + (logfile == null ? "printout" : logfile));
		}
  }

  private static void terminate(String msg) {
    prepTerminate(msg);
		System.exit(0);
	}

  private static void terminate(String msg, int ret) {
    prepTerminate(msg);
		System.exit(ret);
	}
}
