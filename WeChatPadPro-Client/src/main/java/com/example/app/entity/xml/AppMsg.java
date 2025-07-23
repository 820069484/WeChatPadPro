package com.example.app.entity.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true) // 添加此注解忽略未知字段
public class AppMsg {

    @JacksonXmlProperty(isAttribute = true, localName = "appid")
    private String appid;

    @JacksonXmlProperty(isAttribute = true, localName = "sdkver")
    private String sdkver;

    @JacksonXmlProperty(localName = "title")
    @JacksonXmlCData
    private String title;
    
    @JacksonXmlProperty(localName = "des")
    @JacksonXmlCData
    private String description;
    
    @JacksonXmlProperty(localName = "type")
    private int type;
    
    @JacksonXmlProperty(localName = "url")
    @JacksonXmlCData
    private String url;
    
    @JacksonXmlProperty(localName = "thumburl")
    @JacksonXmlCData
    private String thumbUrl;
    
    @JacksonXmlProperty(localName = "wcpayinfo")
    private WcPayInfo wcPayInfo;

}