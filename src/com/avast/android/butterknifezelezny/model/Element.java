package com.avast.android.butterknifezelezny.model;


import com.avast.android.butterknifezelezny.common.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Element {

    // constants
    private static final Pattern sIdPattern = Pattern.compile("@\\+?(android:)?id/([^$]+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern sValidityPattern = Pattern.compile("^([a-zA-Z_\\$][\\w\\$]*)$", Pattern.CASE_INSENSITIVE);
    public String id;
    public boolean isAndroidNS = false;
    public String nameFull; // element name with package
    public String name; // element name
    public String fieldName; // name of variable
    public boolean isValid = false;
    public boolean used = true;
    public boolean isClick = true;

    public Element(String name, String id) {
        // id
        final Matcher matcher = sIdPattern.matcher(id);
        if (matcher.find() && matcher.groupCount() > 0) {
            this.id = matcher.group(2);

            String androidNS = matcher.group(1);
            this.isAndroidNS = !(androidNS == null || androidNS.length() == 0);
        }

        // name
        String[] packages = name.split("\\.");
        if (packages.length > 1) {
            this.nameFull = name;
            this.name = packages[packages.length - 1];
        } else {
            this.nameFull = null;
            this.name = name;
        }

        this.fieldName = getFieldName();
    }

    /**
     * Create full ID for using in layout XML files
     *
     * @return
     */
    public String getFullID() {
        StringBuilder fullID = new StringBuilder();
        String rPrefix;

        if (isAndroidNS) {
            rPrefix = "android.R.id.";
        } else {
            rPrefix = "R.id.";
        }

        fullID.append(rPrefix);
        fullID.append(id);

        return fullID.toString();
    }

    /**
     * Generate field name if it's not done yet
     *
     * @return
     */
    private String getFieldName() {
        String[] words = this.id.split("_");
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.getPrefix());

        for (int i = 0; i < words.length; i++) {
            String[] idTokens = words[i].split("\\.");
            char[] chars = idTokens[idTokens.length - 1].toCharArray();
            if (i > 0 || !Utils.isEmptyString(Utils.getPrefix())) {
                chars[0] = Character.toUpperCase(chars[0]);
            }

            sb.append(chars);
        }

        return sb.toString();
    }

    /**
     * Check validity of field name
     *
     * @return
     */
    public boolean checkValidity() {
        Matcher matcher = sValidityPattern.matcher(fieldName);
        isValid = matcher.find();

        return isValid;
    }
}
