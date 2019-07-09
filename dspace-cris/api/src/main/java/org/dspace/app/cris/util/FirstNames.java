package org.dspace.app.cris.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.dspace.core.ConfigurationManager;

import java.util.HashMap;
import java.util.logging.Level;

/**
 *
 */
public class FirstNames {

    private static FirstNames instance = null;

    private final int capacity = (int) ((49000 + 33000) * 1.2 * 0.8);
    private final boolean loadNameDict = true;
    private final boolean loadYobDict = true;
    private final boolean loadHint = true;
    private final int nameDictStart = 2;
    private final int nameDictLength = 27;
    private int duplicates = 0;     // 20%, factor 0.8

    private HashMap<String, String> map = new HashMap<String, String>(capacity);
    private static final Logger logger = Logger.getLogger(FirstNames.class.getCanonicalName());
    public static String NAME_DICT_FILENAME = "first_names_dict.dat";
    public static String YOB_FILENAME = "first_name_yob2017.dat";

    public static HashMap<String, String> hints = new HashMap<String, String>();
    public static String HINT_FILENAME = "first_name_hint.dat";
    
    private String module = null;

    /**
     * Singleton method
     *
     * @return The FirstNames object
     */
    public static synchronized FirstNames getInstance(String module) {
        if (instance == null) {
            instance = new FirstNames(module);
        }

        return instance;
    }

    /**
     * Initialize hash map.
     *
     */
    private FirstNames(String module) {
    	this.module = module;
    	
        if (loadNameDict) {
            try {
                readNameDict();
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "I cannot read {0} file", getModulePath() + NAME_DICT_FILENAME);
            }
        }
        if (loadYobDict) {
            try {
                readYob();
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "I cannot read {0} file", getModulePath() + YOB_FILENAME);
            }
        }
        if (loadHint) {
            try {
                readHint();
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "I cannot read {0} file", getModulePath() + HINT_FILENAME);
            }
        }
    }

    /**
     * Check if firstName exists on map.
     *
     * @param firstName
     * @return
     */
    public boolean isFirstName(String firstName) {
        return (map.containsKey(firstName.toLowerCase()));
    }

    /**
     * Check if firstName exists on map.
     *
     * @param fullname full name
     * @return First name or null.
     */
    public String getFirstName(String fullname) {
        boolean commaConvention = false;
        if (fullname.contains(",")) {
            // fix fullname
            fullname = fullname.substring(fullname.indexOf(",") + 1).trim();
            commaConvention = true;
        }

        if (fullname.contains(" ")) {
            int last = 0, pos = 0;
            StringBuilder firstName = new StringBuilder(fullname.length());
            firstName.setLength(0);

            while (pos >= 0) {
                pos = fullname.indexOf(" ", last);

                boolean isFirstName = false;
                if (pos >= 0) {
                    isFirstName = isFirstName(fullname.substring(last, pos));
                    if (isFirstName && isStartOfLastName(fullname.substring(last, pos))) {
                        isFirstName = false;
                    }
                } else if (commaConvention) {
                    isFirstName = isFirstName(fullname.substring(last));
                } else if (!commaConvention && firstName.length() > 0) {
                    // last word is part of family name
                    isFirstName = false;
                }

                if (isFirstName) {
                    if (firstName.length() > 0) {
                        firstName.append(" ");
                    }

                    if (pos >= 0) {
                        firstName.append(fullname.substring(last, pos));
                    } else {
                        firstName.append(fullname.substring(last));
                    }
                }

                last = pos + 1;
            }

            return (firstName.length() > 0) ? firstName.toString() : null;
        } else {
            return isFirstName(fullname) ? fullname : null;
        }
    }

    /**
     * Used to optimize capacity.
     *
     * @return
     */
    int getDuplicates() {
        return duplicates;
    }

    /**
     * *
     * Check if a word is a beginning of a last name
     *
     * @param word
     * @return true/false
     */
    private boolean isStartOfLastName(String word) {
        if (hints.containsKey(word.toLowerCase())) {
            return true;
        } else {
            return false;
        }
    }

    /***
     * Path of directory "oai"
     * 
     * @return
     */
    private String getModulePath() {
    	String modFile = ConfigurationManager.getProperty("dspace.dir") +
                File.separator + "config" +
                File.separator + "modules" +
                File.separator + module +
                File.separator;
    	
    	return modFile;
    }
    
    /**
     * Read name_dict.txt file. (GNU Free Documentation License)
     *
     * @see
     * <a href="https://github.com/lead-ratings/gender-guesser/blob/master/gender_guesser/data/nam_dict.txt">
     * List of first names and gender</a>
     */
    private void readNameDict() throws IOException {
    	
        File fis = new File(getModulePath() + NAME_DICT_FILENAME);

        if (fis != null) {
            BufferedReader br = new BufferedReader(new FileReader(fis));

            String s;
            StringBuilder name = new StringBuilder(nameDictLength);
            int pos = 0;
            while ((s = br.readLine()) != null) {
                if ('#' != s.charAt(0)) {
                    name.setLength(0);
                    name.append(s.substring(nameDictStart, nameDictStart + nameDictLength).trim());
                    while ((pos = name.indexOf("+")) >= 0) {
                        name.replace(pos, pos + 1, " ");
                    }
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.log(Level.FINEST, "Name: {0}", name);
                    }

                    String old = map.put(name.toString().toLowerCase(), name.toString());
                    if (old != null) {
                        duplicates++;
                    }
                }
            }
            br.close();
        }
    }

    /**
     *
     * Read yob2017.txt file. (Creative Commons CCZero)
     *
     * @see
     * <a href="https://catalog.data.gov/dataset/baby-names-from-social-security-card-applications-national-level-data">
     * Baby Names from Social Security Card Applications - National Level
     * Data</a>
     */
    private void readYob() throws IOException {
    	File fis = new File(getModulePath() + YOB_FILENAME);

        if (fis != null) {
            BufferedReader br = new BufferedReader(new FileReader(fis));

            String s;
            StringBuilder name = new StringBuilder(100);
            while ((s = br.readLine()) != null) {
                if ('#' != s.charAt(0)) {
                    int end = s.indexOf(",");

                    name.setLength(0);
                    name.ensureCapacity(end);
                    name.append(s.substring(0, end));
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.log(Level.FINEST, "Name: {0}", name);
                    }

                    String old = map.put(name.toString().toLowerCase(), name.toString());
                    if (old != null) {
                        duplicates++;
                    }
                }
            }
            br.close();
        }
    }

    /**
     * Read hint.txt file. *
     */
    private void readHint() throws IOException {
        File fis = new File(getModulePath() + HINT_FILENAME);

        if (fis != null) {
            BufferedReader br = new BufferedReader(new FileReader(fis));

            String hint;
            while ((hint = br.readLine()) != null) {
                if ('#' != hint.charAt(0)) {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.log(Level.FINEST, "Hint: {0}", hint);
                    }

                    hints.put(hint.toLowerCase(), hint);
                }
            }
            br.close();
        }
    }
}