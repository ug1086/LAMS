package server;

import java.util.*;
import components.data.*;
import business.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import org.xml.sax.InputSource;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import javax.jws.*;
import javax.ws.rs.core.*;
import javax.ws.rs.*;


@Path("/Services")
public class LAMSService {

   BusinessLayer businesslayer = new BusinessLayer();
          
   @Context
   private UriInfo context;
   
   @GET
   @Produces("application/xml")
   public String initialize(){
        businesslayer.initialize();
        return businesslayer.getXmlInitializeString(this.context.getBaseUri().toString());
   }
   
   @Path("Appointments")
   @GET
   @Produces("application/xml")
   public String getAllAppointments(){
        List<Object> objs = businesslayer.getAllAppointments();
        if(objs != null){
            return marshalToXML(objs);
        } else {
            return "Appointments don't exist!";
        }
   }
   
   @Path("Appointments/{appointment}")
   @GET
   @Produces("application/xml")
   public String getAppointment(@PathParam("appointment") String appointment){
        List<Object> obj = businesslayer.getAppointment(appointment);
        if(obj != null){
            return marshalToXML(obj);
        } else {
            return "Appointment doesn't exist!";
        }
   } 
   
   @Path("Appointments") 
   @PUT 
   @Consumes({"text/xml","application/xml"}) 
   @Produces("application/xml") 
   public String addAppointment(String xmlStyle){
        List<Object> obj = businesslayer.addAppointment(xmlStyle);
        if(obj != null){
            return marshalToXML(obj);
        } else {
            return errorXML();
        }
   }
   
   @Path("/Labtestinfo/{labtestid}")
   @GET
   @Produces("application/xml")
   public String getTestInfo(@PathParam("labtestid") String labtestid){
        return businesslayer.getTestInfo(labtestid);
   }
   
   public String marshalToXML(List<Object> objs){
   
       try {
           DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
           DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
           
           Document doc = dBuilder.newDocument();
           Element appointmentList = doc.createElement("AppointmentList");
           doc.appendChild(appointmentList);
           
           for(Object obj : objs){
               
              Appointment appointmentObject = (Appointment)obj; 
              Element appointment = doc.createElement("appointment");
              appointment.setAttribute("date", appointmentObject.getApptdate().toString());
              appointment.setAttribute("id", appointmentObject.getId());
              appointment.setAttribute("time", appointmentObject.getAppttime().toString());
              appointmentList.appendChild(appointment);
              
              Element appointmentUri = doc.createElement("uri");
              appointmentUri.appendChild(doc.createTextNode(this.context.getBaseUri().toString()+"Appointments/"+appointmentObject.getId()));
              appointment.appendChild(appointmentUri);
              
              Patient patientObject = appointmentObject.getPatientid();
              Element patient = doc.createElement("patient");
              patient.setAttribute("id", patientObject.getId());
              Element patientUri = doc.createElement("uri");
              patient.appendChild(patientUri);
              Element patientName = doc.createElement("name");
              patientName.appendChild(doc.createTextNode(patientObject.getName()));
              patient.appendChild(patientName);
              appointment.appendChild(patient);
              Element patientAddress = doc.createElement("address");
              patientAddress.appendChild(doc.createTextNode(patientObject.getAddress()));
              patient.appendChild(patientAddress);
              Element patientInsurance = doc.createElement("insurance");
              patientInsurance.appendChild(doc.createTextNode(String.valueOf(patientObject.getInsurance())));
              patient.appendChild(patientInsurance);
              Element patientDob = doc.createElement("dob");
              patientDob.appendChild(doc.createTextNode(patientObject.getDateofbirth().toString()));
              patient.appendChild(patientDob);
              
              Phlebotomist phleObject = appointmentObject.getPhlebid();
              Element phlebotomist = doc.createElement("phlebotomist");
              phlebotomist.setAttribute("id", phleObject.getId());
              appointment.appendChild(phlebotomist);
              Element phelebotomistUri = doc.createElement("uri");
              phlebotomist.appendChild(phelebotomistUri);
              Element phlebotomistName = doc.createElement("name");
              phlebotomistName.appendChild(doc.createTextNode(phleObject.getName()));
              phlebotomist.appendChild(phlebotomistName);
              
              PSC pscObject = appointmentObject.getPscid();
              Element psc = doc.createElement("psc");
              psc.setAttribute("id", pscObject.getId());
              appointment.appendChild(psc);
              Element pscUri = doc.createElement("uri");
              psc.appendChild(pscUri);
              Element pscName = doc.createElement("name");
              pscName.appendChild(doc.createTextNode(pscObject.getName()));
              psc.appendChild(pscName);
              
              List<AppointmentLabTest> labTests = appointmentObject.getAppointmentLabTestCollection();
              
              Element allLabTests = doc.createElement("allLabTests");
              appointment.appendChild(allLabTests);
              
              for(AppointmentLabTest labTest : labTests){
                  Element appointmentLabTest = doc.createElement("appointmentLabTest");
                  allLabTests.appendChild(appointmentLabTest);
                  appointmentLabTest.setAttribute("appointmentId", labTest.getAppointmentLabTestPK().getApptid());
                  appointmentLabTest.setAttribute("dxcode", labTest.getAppointmentLabTestPK().getDxcode());
                  appointmentLabTest.setAttribute("labTestId", labTest.getAppointmentLabTestPK().getLabtestid());
                  Element appointmentLabTestUri = doc.createElement("uri");
                  appointmentLabTest.appendChild(appointmentLabTestUri);
              }
              
           }
           
           DOMSource source = new DOMSource(doc);
           StringWriter writer = new StringWriter();
           StreamResult result = new StreamResult(writer);
           
           TransformerFactory transformerFactory = TransformerFactory.newInstance();
           Transformer transformer = transformerFactory.newTransformer();
         
           transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
           //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
           transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
           transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
           transformer.transform(source, result);
           String strResult = writer.toString();
           //System.out.println(strResult);
           return strResult;
           
         } catch (Exception e) {
		      e.printStackTrace();
	      }
         return "";
      }
      
      public String errorXML(){
   
       try {
           DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
           DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
           
           Document doc = dBuilder.newDocument();
           Element appointmentList = doc.createElement("AppointmentList");
           doc.appendChild(appointmentList);
           
           Element error = doc.createElement("error");
           error.appendChild(doc.createTextNode("ERROR: Appointment is not available"));
           appointmentList.appendChild(error);
           
           DOMSource source = new DOMSource(doc);
           StringWriter writer = new StringWriter();
           StreamResult result = new StreamResult(writer);
           
           TransformerFactory transformerFactory = TransformerFactory.newInstance();
           Transformer transformer = transformerFactory.newTransformer();
         
           transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
           //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
           transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
           transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
           transformer.transform(source, result);
           String errorResult = writer.toString();
           return errorResult;
            
         } catch (Exception e) {
		      e.printStackTrace();
	      }
         return "";
      }


}