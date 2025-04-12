package utility;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {

    public static ModelView responseApi(String status, int code, Map<String, Object> data, Object error) {
        ModelView modelView = new ModelView();

        modelView.addData("status",status);
        modelView.addData("code", code);
        modelView.addData("data", data);
        modelView.addData("error", error);

        return modelView;
    }

    public static ModelView successApi(Map<String, Object> data,int code) {
        return responseApi(
            "success", 
            code, 
            data, 
            null
        );
    }

    public static ModelView errorApi(int code, String message, String details) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("message", message);
        errorDetails.put("details", details);

        return responseApi(
            "error", 
            code, 
            null, 
            errorDetails
        );
    }
}
