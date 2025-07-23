package com.example.app.entity.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

@JacksonXmlRootElement(localName = "msg")
@Getter
@Setter
public class WechatTransferMsg {
    @JacksonXmlProperty(localName = "appmsg")
    private AppMsg appMsg;
}