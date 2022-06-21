package io.camunda.operate.schema.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

public final class SemanticVersion implements Comparable<SemanticVersion> {
   private static Pattern splitPattern = Pattern.compile("\\.(?=\\d)");
   private final List versionParts;
   private final String displayText;

   public static SemanticVersion fromVersion(String version) {
      return new SemanticVersion(version);
   }

   public boolean isBetween(SemanticVersion olderVersion, SemanticVersion newerVersion) {
      return this.isNewerThan(olderVersion) && !this.isNewerThan(newerVersion);
   }

   public SemanticVersion(String version) {
      if (StringUtils.isEmpty(version)) {
         throw new IllegalArgumentException("version should not be null or empty");
      } else if (StringUtils.containsWhitespace(version)) {
         throw new IllegalArgumentException("version should not contain white space");
      } else {
         String normalizedVersion = version.replace('_', '.');
         if (normalizedVersion.toLowerCase().indexOf("-") > 0) {
            normalizedVersion = normalizedVersion.substring(0, normalizedVersion.toLowerCase().indexOf("-"));
         }

         this.versionParts = this.tokenize(normalizedVersion);
         this.displayText = (String)this.versionParts.stream().map(Object::toString).collect(Collectors.joining("."));
      }
   }

   public String toString() {
      return this.displayText;
   }

   public String getVersion() {
      return this.displayText;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SemanticVersion version1 = (SemanticVersion)o;
         return this.compareTo(version1) == 0;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.versionParts == null ? 0 : this.versionParts.hashCode();
   }

   public boolean isAtLeast(String otherVersion) {
      return this.compareTo(fromVersion(otherVersion)) >= 0;
   }

   public boolean isNewerThan(String otherVersion) {
      return this.compareTo(fromVersion(otherVersion)) > 0;
   }

   public boolean isNewerThan(SemanticVersion otherVersion) {
      return this.compareTo(otherVersion) > 0;
   }

   public boolean isMajorNewerThan(String otherVersion) {
      return this.isMajorNewerThan(fromVersion(otherVersion));
   }

   public boolean isMajorNewerThan(SemanticVersion otherVersion) {
      return this.getMajor().compareTo(otherVersion.getMajor()) > 0;
   }

   public Integer getMajor() {
      return (Integer)this.versionParts.get(0);
   }

   public String getMajorAsString() {
      return ((Integer)this.versionParts.get(0)).toString();
   }

   public Integer getMinor() {
      return this.versionParts.size() == 1 ? 0 : (Integer)this.versionParts.get(1);
   }

   public String getMinorAsString() {
      return this.versionParts.size() == 1 ? "0" : ((Integer)this.versionParts.get(1)).toString();
   }

   @Override
   public int compareTo(SemanticVersion o) {
      if (o == null) {
         return 1;
      } else {
         List parts1 = this.versionParts;
         List parts2 = o.versionParts;
         int largestNumberOfParts = Math.max(parts1.size(), parts2.size());

         for(int i = 0; i < largestNumberOfParts; ++i) {
            int compared = this.getOrZero(parts1, i).compareTo(this.getOrZero(parts2, i));
            if (compared != 0) {
               return compared;
            }
         }

         return 0;
      }
   }

   private Integer getOrZero(List elements, int i) {
      return i < elements.size() ? (Integer)elements.get(i) : 0;
   }

   private List tokenize(String version) {
      List parts = new ArrayList();

      try {
         String[] var3 = splitPattern.split(version);
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String part = var3[var5];
            parts.add(Integer.valueOf(part));
         }
      } catch (NumberFormatException var7) {
         throw new RuntimeException("Invalid version containing non-numeric characters. Only 0..9 and . are allowed. Invalid version: " + version);
      }

      for(int i = parts.size() - 1; i > 0 && ((Integer)parts.get(i)).equals(0); --i) {
         parts.remove(i);
      }

      return parts;
   }

}
