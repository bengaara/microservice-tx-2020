package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.tospay.transaction.models.BaseModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.tospay.transaction.util.Utils;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LicenseObject<T> extends BaseModel {
    @JsonProperty("vendor")
    private String vendor;

    @JsonProperty("processorSerialNumber")
    private String processorSerialNumber;

    @JsonProperty("processorIdentifier")
    private String processorIdentifier;

    @JsonProperty("processors")
    private int processors;

    @JsonProperty("systemUpTime")
    private Long systemUpTime;

    @JsonProperty("launchDate")
    private Date launchDate;
    @JsonProperty("systemBootTime")
    private Long systemBootTime;
    @JsonProperty("serviceId")
    private String serviceId;
    @JsonProperty("macAddress")
    private List<String> macAddress = new ArrayList<String>();

    @JsonProperty("ipv4Address")
    private List<String> ipv4Address = new ArrayList<String>();
    @JsonProperty("ipv6Address")
    private List<String> ipv6Address = new ArrayList<String>();

    public Date getLaunchDate() {
        return launchDate;
    }

    public void setLaunchDate(Date launchDate) {
        this.launchDate = launchDate;
    }

    public Long getSystemUpTime() {
        return systemUpTime;
    }

    public void setSystemUpTime(Long systemUpTime) {
        this.systemUpTime = systemUpTime;
    }

    public Long getSystemBootTime() {
        return systemBootTime;
    }

    public void setSystemBootTime(Long systemBootTime) {
        this.systemBootTime = systemBootTime;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public List<String> getIpv4Address() {
        return ipv4Address;
    }

    public void setIpv4Address(List<String> ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    public List<String> getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(List<String> ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getProcessorSerialNumber() {
        return processorSerialNumber;
    }

    public void setProcessorSerialNumber(String processorSerialNumber) {
        this.processorSerialNumber = processorSerialNumber;
    }

    public String getProcessorIdentifier() {
        return processorIdentifier;
    }

    public void setProcessorIdentifier(String processorIdentifier) {
        this.processorIdentifier = processorIdentifier;
    }

    public int getProcessors() {
        return processors;
    }

    public void setProcessors(int processors) {
        this.processors = processors;
    }

    public List<String> getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(List<String> macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public String toString() {
        String s = "";
        s = Utils.inspect(this);
       // logger.debug("toString toggling hideData to {} {}", hideData, Utils.getCallerCallerClassName());
        return s;
    }
}
