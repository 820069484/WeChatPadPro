package com.example.app.entity.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WcPayInfo {
    @JacksonXmlProperty(localName = "paysubtype")
    private int paySubType;
    
    @JacksonXmlProperty(localName = "feedesc")
    @JacksonXmlCData
    private String feeDesc;
    
    @JacksonXmlProperty(localName = "transcationid")
    @JacksonXmlCData
    private String transactionId;
    
    @JacksonXmlProperty(localName = "transferid")
    @JacksonXmlCData
    private String transferId;
    
    @JacksonXmlProperty(localName = "invalidtime")
    private long invalidTime;
    
    @JacksonXmlProperty(localName = "begintransfertime")
    private long beginTransferTime;
    
    @JacksonXmlProperty(localName = "receiver_username")
    @JacksonXmlCData
    private String receiverUsername;

}