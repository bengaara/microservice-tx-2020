package net.tospay.transaction.controllers;

import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.util.Constants;
import net.tospay.transaction.util.Constants.URL;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(URL.API_VER)
public class LicenseController extends BaseController {

    public LicenseController() {

    }

    @GetMapping(URL.LICENSE_ENABLE)
    public ResponseObject<BaseModel> enable() {

        logger.debug(" license enable {}");

        Constants.LICENSE_ACTIVE = true;
        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            null);//return mapResponse(response);
    }

    @GetMapping(URL.LICENSE_DISABLE)
    public ResponseObject<BaseModel> disable() {

        logger.debug(" license disable {}");
        Constants.LICENSE_ACTIVE = false;

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            null);//return mapResponse(response);
    }
}
