package org.evolve.common.config;

import org.mapstruct.control.MappingControl;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

/***
 * Mapstruct配置
 * @author gm
 * @time 2026/4/9
 */
public class MapstructConfig {
    /**
     * BaseConverter
     */
    public interface BaseConverter {
        /**
         * dateToLocalDateTime
         * @param date date
         * @return LocalDateTime
         */
        static LocalDateTime dateToLocalDateTime(Date date) {
            return date == null ? null : date.toInstant().atZone(TimeZone.getTimeZone("GMT+8").toZoneId()).toLocalDateTime();
        }

        /**
         * dateToLocalDate
         * @param date date
         * @return LocalDate
         */
        static LocalDate dateToLocalDate(Date date) {
            return date == null ? null : date.toInstant().atZone(TimeZone.getTimeZone("GMT+8").toZoneId()).toLocalDate();
        }


        /**
         * localDateToDate
         * @param localDate localDate
         * @return Date
         */
        static Date localDateToDate(LocalDate localDate) {
            return localDate == null ? null :
                    Date.from(localDate.atStartOfDay(TimeZone.getTimeZone("GMT+8").toZoneId()).toInstant());
        }

        /**
         * localDateTimeToDate
         * @param localDateTime localDateTime
         * @return Date
         */
        static Date localDateTimeToDate(LocalDateTime localDateTime) {
            return localDateTime == null ? null :
                    Date.from(localDateTime.atZone(TimeZone.getTimeZone("GMT+8").toZoneId()).toInstant());
        }

    }

    /**
     * 深拷贝注解
     */
    @Retention(RetentionPolicy.CLASS)
    @MappingControl(MappingControl.Use.BUILT_IN_CONVERSION)
    @MappingControl(MappingControl.Use.MAPPING_METHOD)
    @MappingControl(MappingControl.Use.COMPLEX_MAPPING)
    public @interface DeepCopyMapping {
    }
}
