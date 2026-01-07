package ae.dar360.user.helper;

import ae.dar360.user.constant.AppStatusConstant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AppStatus <T>{
    String code;
    String message;
    Boolean isError;
    T data;
    Long timeStamp;

    public AppStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public AppStatus(String code, String message, Boolean isError) {
        this(code, message);
        this.isError = isError;
    }

    public AppStatus(String code, String message, Boolean isError, T data) {
        this(code, message, isError, data, System.currentTimeMillis());
    }


    public static AppStatus ofOk(String message, Object data) {
        return new AppStatus(AppStatusConstant.Code.ok, AppStatusConstant.Message.ok, Boolean.FALSE, data);
    }

    public static AppStatus ofInternalServerError(String message, Object data) {
        return new AppStatus(AppStatusConstant.Code.internalSeverError, AppStatusConstant.Message.internalSeverError, Boolean.TRUE, data);
    }
}
