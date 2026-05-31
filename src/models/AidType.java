package models;

import java.util.Arrays;
import java.util.List;

public enum AidType {
    FOOD("طعام", "مواد غذائية"),
    MEDICINE("دواء", "أدوية ومستلزمات طبية"),
    SHELTER("إيواء", "خيام ومستلزمات إيواء"),
    CLOTHING("ملابس", "ملابس وبطانيات"),
    WATER("مياه", "مياه شرب"),
    HYGIENE("نظافة", "مواد تنظيف وتعقيم"),
    EDUCATION("تعليم", "قرطاسية وحقائب مدرسية"),
    CASH("مساعدات نقدية", "مبالغ مالية"),
    OTHER("أخرى", "مساعدة أخرى");
    
    private final String arabicName;
    private final String description;
    
    AidType(String arabicName, String description) {
        this.arabicName = arabicName;
        this.description = description;
    }
    
    public String getArabicName() {
        return arabicName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static List<AidType> getAllTypes() {
        return Arrays.asList(values());
    }
    
    public static String[] getNames() {
        return Arrays.stream(values())
                .map(AidType::getArabicName)
                .toArray(String[]::new);
    }
    
    public static AidType fromArabicName(String arabicName) {
        for (AidType type : values()) {
            if (type.arabicName.equals(arabicName)) {
                return type;
            }
        }
        return OTHER;
    }
    
    @Override
    public String toString() {
        return arabicName;
    }
}