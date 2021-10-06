package org.telegram.messenger.Utils;

import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

public class DateConvertor {
    public static String longToDateString(Long date) {
        PersianDate persianDate = new PersianDate(date);
        PersianDateFormat format = new PersianDateFormat("j F y");
        return format.format(persianDate);
    }
}
