package barinov.task.management.system.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ValidConvertStringToEnum<T extends Enum<T>> {
    private final Class<T> enumClass;

    public ValidConvertStringToEnum(Class<T> typeParameterClass) {
        this.enumClass = typeParameterClass;
    }

    public boolean isValidate(String string) {
        if (string == null) return false;

        try {
            Enum.valueOf(enumClass, string);
        }catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

    public String valuesEnumToString() {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::toString)
                .collect(Collectors.joining(", "));
    }
}
