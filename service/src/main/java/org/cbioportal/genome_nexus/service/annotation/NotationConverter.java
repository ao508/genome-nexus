package org.cbioportal.genome_nexus.service.annotation;

import org.cbioportal.genome_nexus.model.GenomicLocation;
import org.springframework.stereotype.Component;

@Component
public class NotationConverter
{
    public GenomicLocation parseGenomicLocation(String genomicLocation, String delimiter)
    {
        String[] parts = genomicLocation.split(delimiter);
        GenomicLocation location = null;

        if (parts.length >= 5)
        {
            location = new GenomicLocation();

            location.setChromosome(parts[0]);
            location.setStart(Integer.parseInt(parts[1]));
            location.setEnd(Integer.parseInt(parts[2]));
            location.setReferenceAllele(parts[3]);
            location.setVariantAllele(parts[4]);
        }

        return location;
    }

    public String genomicToHgvs(GenomicLocation genomicLocation)
    {
        String chr = genomicLocation.getChromosome();
        Integer start = genomicLocation.getStart();
        Integer end = genomicLocation.getEnd();
        String ref = genomicLocation.getReferenceAllele();
        String var = genomicLocation.getVariantAllele();

        String prefix = "";

        if(!ref.equals(var)) {
            prefix = longestCommonPrefix(ref, var);
        }
//        else {
//            log.warn("Warning: Reference allele extracted from " + chr + ":" + start + "-" + end + " matches alt allele.");
//        }

        // Remove common prefix and adjust variant position accordingly
        if (prefix.length() > 0)
        {
            ref = ref.substring(prefix.length());
            var = var.substring(prefix.length());

            int nStart = start;
            int nEnd = end;

            nStart += prefix.length();

            if (ref.length() == 0) {
                nStart -= 1;
            }

            start = nStart;
        }

        String hgvs;

        /*
         Process Insertion
         Example insertion: 17 36002277 36002278 - A
         Example output: 17:g.36002277_36002278insA
         */
        if(ref.equals("-") || ref.length() == 0)
        {
            try {
                hgvs = chr + ":g." + start + "_" + String.valueOf(start + 1) + "ins" + var;
            }
            catch (NumberFormatException e) {
                return "";
            }
        }
        /*
         Process Deletion
         Example deletion: 1 206811015 206811016  AC -
         Example output:   1:g.206811015_206811016delAC
         */
        else if(var.equals("-") || var.length() == 0) {
            hgvs = chr + ":g." + start + "_" + end + "del" + ref;
        }
        /*
         Process ONP
         Example SNP   : 2 216809708 216809709 CA T
         Example output: 2:g.216809708_216809709delCAinsT
         */
        else if (ref.length() > 1) {
            hgvs = chr + ":g." + start + "_" + end + "del" + ref + "ins" + var;
        }
        /*
         Process SNV
         Example SNP   : 2 216809708 216809708 C T
         Example output: 2:g.216809708C>T
         */
        else {
            hgvs = chr + ":g." + start + ref + ">" + var;
        }

        return hgvs;
    }

    // TODO factor out to a utility class as a static method if needed
    public String longestCommonPrefix(String str1, String str2)
    {
        for (int prefixLen = 0; prefixLen < str1.length(); prefixLen++)
        {
            char c = str1.charAt(prefixLen);

            if (prefixLen >= str2.length() ||
                str2.charAt(prefixLen) != c)
            {
                // mismatch found
                return str2.substring(0, prefixLen);
            }
        }

        return str1;
    }
}
