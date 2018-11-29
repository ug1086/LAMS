package server;

import server.*;
import java.util.*;
import components.data.*;
import business.*;

public class TestDB {
   
    public static void main(String[] args) {
      LAMSService service = new LAMSService();
      BusinessLayer bl = new BusinessLayer();
      //service.initialize();
      //System.out.println(service.getAllAppointments());
      //System.out.println(service.getAppointment("798"));
      
      String xml= "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>"+
                  "<appointment>"+
                   "<date>2018-11-28</date>"+
                   "<time>10:00:00</time>"+
                   "<patientId>210</patientId>"+
                   "<physicianId>30</physicianId>"+
                   "<pscId>500</pscId>"+
                   "<phlebotomistId>120</phlebotomistId>"+
                   "<labTests>"+
                    "<test id=\"80200\" dxcode=\"290.0\" />"+
                   "</labTests>"+
                  "</appointment>";
               
      //System.out.println(service.addAppointment(xml));
      //System.out.println(bl.getNextAppointmentId());
      //System.out.println(bl.errorXML());
      //System.out.println(service.getTestInfo("80200"));
      //System.out.println(bl.isDxcodeValid("200"));
      System.out.println(bl.isAppointmentAvailable("2018-09-28", "10:00:00", "520"));
      //System.out.println(bl.getPatientObject("210", "30"));
      //System.out.println(bl.isPhlebotomistAvailable("2017-02-01", "11:20:00", "120", "500"));
      //System.out.println(bl.isAppointmentTimeInRange("17:00:00"));
    }
}