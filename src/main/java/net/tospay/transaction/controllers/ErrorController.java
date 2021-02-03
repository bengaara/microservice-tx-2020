package net.tospay.transaction.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseErrorObject;
import net.tospay.transaction.models.response.ResponseObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ErrorController extends ResponseEntityExceptionHandler {
    ObjectMapper mapper = new ObjectMapper();


    public ErrorController() {

    }

    public <T extends ResponseObject> ResponseEntity mapResponse(T response) {

        try {
            if (!ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus())) {//on error map?
                String status =
                        response.getStatus() != null ? response.getStatus() : ResponseCode.FAILURE.type;
                String description = response.getDescription() != null ? response.getDescription() :
                        ResponseCode.FAILURE.name();
                ArrayList<Error> errors = new ArrayList<>();
                Error error = new Error(status, description);
                errors.add(error);
                switch (ResponseCode.valueOfType(response.getStatus())) {

                    //case GENERAL_ERROR:
                    default:
                        status =
                                response.getStatus() != null ? response.getStatus() : ResponseCode.FAILURE.type;
                        description = response.getDescription() != null ? response.getDescription() :
                                ResponseCode.FAILURE.name();
                        errors = new ArrayList<>();
                        error = new Error(status, description);
                        errors.add(error);

                        break;
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

//    @ExceptionHandler(value = HttpMessageNotReadableException.class)
//    // @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity handleJsonMappingException(HttpMessageNotReadableException e) {
//        logger.error("", e);
//
//        Error error = new Error();
//        error.setCode(ResponseCode.GENERAL_ERROR.type);
//        error.setDescription("Internal server error");
////
////
////        String er = e.getStackTrace()[0].getLineNumber() + " - " + e.getStackTrace()[0].getClassName()
////                + e.getStackTrace()[0].getMethodName();
////        error.setStackTrace(er);
//
//
//        ResponseObject response = new ResponseObject(ResponseCode.FAILURE.type,
//            ResponseCode.FAILURE.name(),
//            Arrays.asList(new Error(ResponseCode.GENERAL_ERROR.type, ResponseCode.GENERAL_ERROR.name()))
//            , null);//NestedExceptionUtils.getMostSpecificCause( e));
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//
//    }
    @ExceptionHandler(value = Exception.class)
    @SuppressWarnings("unchecked")
    //public <T extends ResponseObject> ResponseObject<T> exception(Exception e) {
    public ResponseEntity exception(Exception e) {
        logger.error("", e);

        Error error = new Error();
        error.setCode(ResponseCode.GENERAL_ERROR.type);
        error.setDescription("Internal server error");
//
//
//        String er = e.getStackTrace()[0].getLineNumber() + " - " + e.getStackTrace()[0].getClassName()
//                + e.getStackTrace()[0].getMethodName();
//        error.setStackTrace(er);


        ResponseObject response = new ResponseObject(ResponseCode.FAILURE.type,
                ResponseCode.FAILURE.name(),
                Arrays.asList(new Error(ResponseCode.GENERAL_ERROR.type, ResponseCode.GENERAL_ERROR.name()))
                , null);//NestedExceptionUtils.getMostSpecificCause( e));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(value = ResponseErrorObject.class)
    public ResponseEntity exception(ResponseErrorObject response) {
        // logger.error("", response);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getResponse());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        List<String> validationList = ex.getBindingResult().getFieldErrors().stream().map(fieldError -> fieldError.getDefaultMessage()).collect(Collectors.toList());
        logger.info("Validation error list : " + validationList);
        Error error = new Error();
        error.setCode(ResponseCode.GENERAL_ERROR.type);
        error.setDescription("Internal server error");

        ResponseObject response = new ResponseObject(ResponseCode.FAILURE.type,
                ResponseCode.FAILURE.name(),
                Arrays.asList(new Error(ResponseCode.GENERAL_ERROR.type, ResponseCode.GENERAL_ERROR.name()))
                , validationList);//NestedExceptionUtils.getMostSpecificCause( e));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
