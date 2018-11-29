package business;

import java.util.*;
import server.*;
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
import java.time.format.*;


public class BusinessLayer {

      private DBSingleton dbSingleton = DBSingleton.getInstance();
   
      final static String DATE_FORMAT = "yyyy-MM-dd";

      public String initialize(){
//         dbSingleton = DBSingleton.getInstance();
         dbSingleton.db.initialLoad("LAMS");
         return "Database initialized!";
      }
      
      public String getXmlInitializeString(String xmlInitializeString){
         return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                 "<AppointmentList>"+
                 "<intro>Welcome to the LAMS Appointment Service</intro>"+
                 "<wadl>"+xmlInitializeString+"application.wadl</wadl>"+
                 "</AppointmentList>";
      }
      
      public List<Object> getAllAppointments(){
           String appointments = "";
           // dbSingleton = DBSingleton.getInstance();
           System.out.println("All appointments");
           List<Object> objs = dbSingleton.db.getData("Appointment", "");
           if(objs.isEmpty()){
                 initialize();
                 objs = dbSingleton.db.getData("Appointment", "");
           }
           //appointments = marshalToXML(objs);
           return objs;
      }
      
      public List<Object> getAppointment(String appointNumber){
           String appointment = "";
          //  dbSingleton = DBSingleton.getInstance();
           List<Object> obj = dbSingleton.db.getData("Appointment", "id='"+appointNumber+"'");   
             if(obj.isEmpty()){
                 initialize();
                 obj = dbSingleton.db.getData("Appointment", "id='"+appointNumber+"'");
             }
             if(!obj.isEmpty()){
                 //appointment = marshalToXML(obj);
                 return obj;
             } else {
                 //appointment = "Appointment doesn't exist!";
                 return null;
             }
      }
      
      public String getTestInfo(String labtestid){
           // dbSingleton = DBSingleton.getInstance();
            List<Object> TestList = dbSingleton.db.getData("LabTest", "id='"+labtestid+"'");
            if(!TestList.isEmpty()){
            LabTest LabTestObj = (LabTest) TestList.get(0);
            
         try {
              DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
              DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
              
              Document doc = dBuilder.newDocument();
              Element labTestList = doc.createElement("LabTestList");
              doc.appendChild(labTestList);
              
              Element labtest = doc.createElement("ID");
              labtest.appendChild(doc.createTextNode(LabTestObj.getId()));
              labTestList.appendChild(labtest);
              
              Element labtestname = doc.createElement("name");
              labtestname.appendChild(doc.createTextNode(LabTestObj.getName()));
              labTestList.appendChild(labtestname);
              
              Element labtestcost = doc.createElement("cost");
              labtestcost.appendChild(doc.createTextNode(String.valueOf(LabTestObj.getCost())));
              labTestList.appendChild(labtestcost);
              
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
              String labTestResult = writer.toString();
              return labTestResult;
               
            } catch (Exception e) {
   		      e.printStackTrace();
               return "Lab test not found!";
   	      }
         } else {
            return "Lab test not found!";
         }
      }
      
 
      public List<Object> addAppointment(String xmlStyle){
      
      // dbSingleton = DBSingleton.getInstance();
      LAMSService service = new LAMSService();
      
      try{
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(new InputSource(new StringReader(xmlStyle))); 
         doc.getDocumentElement().normalize();
         Element root = doc.getDocumentElement(); //get root
         //System.out.println("Root Element: "+root.getNodeName());
         
         String error = "";
         
         NodeList nList = doc.getElementsByTagName("appointment");
            for(int temp=0; temp<nList.getLength(); temp++){
               Node nNode = nList.item(temp);
               //System.out.println("\nCurrent Element: "+nNode.getNodeName());
               
               if(nNode.getNodeType() == Node.ELEMENT_NODE){
               Element element = (Element) nNode;
               
               String date = element.getElementsByTagName("date").item(0).getTextContent();
               //System.out.println("Date: "+date);
               String time = element.getElementsByTagName("time").item(0).getTextContent();
               String patientId = element.getElementsByTagName("patientId").item(0).getTextContent();
               String physicianId = element.getElementsByTagName("physicianId").item(0).getTextContent();
               String pscId = element.getElementsByTagName("pscId").item(0).getTextContent();
               String phlebotomistId = element.getElementsByTagName("phlebotomistId").item(0).getTextContent();
               Patient patient = null;
               Phlebotomist phlebotomist = null;
               PSC psc = null;
               
               if(isIdValid(patientId) && isIdValid(phlebotomistId) && isIdValid(pscId)){
                  patient = getPatientObject(patientId, physicianId);
                  phlebotomist = getPhlebotomistObject(phlebotomistId);
                  psc = getPSCObject(pscId);
                                 
                  String apptid = getNextAppointmentId();
                  Appointment newAppt = null;
                  
                  if(isDateValid(date) && isTimeValid(time)){
                  
                     if(isAppointmentAvailable(date, time, patientId)){
                        
                        if(isPhlebotomistAvailable(date, time, phlebotomistId, pscId)){
                  
                           List<AppointmentLabTest> tests = new ArrayList<AppointmentLabTest>();
                                                   
                           NodeList labTestList = element.getElementsByTagName("test");
                           for(int i=0; i<labTestList.getLength(); i++){
                              Node node = (Node)labTestList.item(i);
                              Element e = (Element)node;
                              String[] labTestId = new String[labTestList.getLength()];
                              labTestId[i] = e.getAttribute("id");
                              String[] dxcode = new String[labTestList.getLength()];
                              dxcode[i] = e.getAttribute("dxcode");
                              
                              if(isIdValid(labTestId[i]) && isDxcodeValid(dxcode[i])){
                                 AppointmentLabTest test = new AppointmentLabTest(apptid, labTestId[i], dxcode[i]);
                                 Diagnosis diagnosis = getDiagnosisObject(dxcode[i]);
                                 test.setDiagnosis(diagnosis);
         
                                 LabTest labtest = getLabTestObject(labTestId[i]);
                                 if(labtest!=null){
                                    test.setLabTest(labtest);
                                    tests.add(test);
                                 } else {
                                    return null;
                                 }
                              } else {
                                 return null;
                              }
         
                           } //each test
                           
                           newAppt = new Appointment(apptid, java.sql.Date.valueOf(date), java.sql.Time.valueOf(time));
                           
                           if(patient!=null && phlebotomist!=null && psc!=null && !tests.isEmpty()){
                              newAppt.setAppointmentLabTestCollection(tests);
                              newAppt.setPatientid(patient);
                              newAppt.setPhlebid(phlebotomist);
                              newAppt.setPscid(psc);
                              
                              boolean good = dbSingleton.db.addData(newAppt);
                              
                              if(good){
                                 return getAppointment(apptid);
                              } else {
                                 return null;
                                }
                           } else {
                              return null;
                             }
                        } else {
                           return null;
                          }                     
                     } else {
                        return null;
                       }
                  } else {
                     return null;
                    }
               } else {
                     return null;
                 }
                     
               } else {
                     return null;
               }//element node
            } //appointment
         
            System.out.println("--------------------------------\n\n");
            
         } catch (Exception e){
            e.printStackTrace();
         }
         return null;
      }


      public Patient getPatientObject(String patientId, String physicianId){
         // dbSingleton = DBSingleton.getInstance();
         List<Object> patientList = dbSingleton.db.getData("Patient", "id='"+patientId+"'");
         if(!patientList.isEmpty()){
            Patient patientObj = (Patient) patientList.get(0);
            if((patientObj.getPhysician().getId()).equals(physicianId)){
               System.out.println("physician found");
               return patientObj;
            }
         } else {
            return null;
         }
         return null;
      }

      public Phlebotomist getPhlebotomistObject(String phlebotomistId){
         // dbSingleton = DBSingleton.getInstance();
         List<Object> phlebotomistList = dbSingleton.db.getData("Phlebotomist", "id='"+phlebotomistId+"'");
         if(!phlebotomistList.isEmpty()){
            Phlebotomist phlebotomistObj = (Phlebotomist) phlebotomistList.get(0);
            System.out.println(phlebotomistObj.toString());
            return phlebotomistObj;
         } else {
            return null;
         }
      }
   

      public PSC getPSCObject(String pscId){
        //  dbSingleton = DBSingleton.getInstance();
         List<Object> pscList = dbSingleton.db.getData("PSC", "id='"+pscId+"'");
         if(!pscList.isEmpty()){
            PSC pscObj = (PSC) pscList.get(0);
            System.out.println(pscObj.toString());
            return pscObj;
         } else {
            return null;
         }
      }
   
      public String getNextAppointmentId(){
//          dbSingleton = DBSingleton.getInstance();
         List<Object> objs = dbSingleton.db.getData("Appointment", "id=(select max(id) from Appointment)");
         if(objs.isEmpty()){
              initialize();
              objs = dbSingleton.db.getData("Appointment", "id=(select max(id) from appointment)");
         }
         //Object o = objs.get(objs.size()-1);
         Appointment lastAppointment = (Appointment) objs.get(0);
         int id = Integer.parseInt(lastAppointment.getId())+1;
         return Integer.toString(id);
      }
   
      public Diagnosis getDiagnosisObject(String dxcode){
        //  dbSingleton = DBSingleton.getInstance();
         List<Object> DiagnosisList = dbSingleton.db.getData("Diagnosis", "code='"+dxcode+"'");
         if(!DiagnosisList.isEmpty()){
            Diagnosis DiagnosisObj = (Diagnosis) DiagnosisList.get(0);
            System.out.println(DiagnosisObj.toString());
            return DiagnosisObj;
         } else {
            return null;
         }
      }
      
      public LabTest getLabTestObject(String labtest){
         // dbSingleton = DBSingleton.getInstance();
         List<Object> labTestList = dbSingleton.db.getData("LabTest", "id='"+labtest+"'");
         if(!labTestList.isEmpty()){
            LabTest LabTestObj = (LabTest) labTestList.get(0);
            System.out.println(LabTestObj.toString());
            return LabTestObj;
         } else {
            return null;
         }
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
         return "hello";
      }
   
      public static boolean isDateValid(String date) {
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
      }
   
      public static boolean isTimeValid(String time) {
       try {
           DateTimeFormatter strictTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
           .withResolverStyle(ResolverStyle.STRICT);
           LocalTime.parse(time, strictTimeFormatter);
           System.out.println("Valid time string: " + time);
           return true;
       } catch (Exception e) {
           System.out.println("Invalid time string: " + time);
           return false;
       }
      }
    
      public static boolean isIdValid(String id) {
         if(!id.matches("[0-9]+") || id.matches("^[0]+$") || id==""){
            return false;
         } else {
            return true;
         } 
      }
   
      public static boolean isDxcodeValid(String dxcode) {
         if(!dxcode.matches("^[0-9]*\\.?[0-9]*$") || dxcode.matches("^[0]+$") || dxcode=="") {
             return false;
         }
         else {
             return true;
         }
      }
      
      public boolean isAppointmentAvailable(String date, String time, String patientId){
       try{
         if(isAppointmentTimeInRange(time)){
            if(!isAppointmentDuplicate(date, time, patientId)){
               String tempTime = time;
               // dbSingleton = DBSingleton.getInstance();
               SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
               Date d = df.parse(tempTime); 
               Calendar cal = Calendar.getInstance();
               cal.setTime(d);
               cal.add(Calendar.MINUTE, -15);
               String time1 = df.format(cal.getTime());
               
               Calendar cal1 = Calendar.getInstance();
               cal1.setTime(d);
               cal.add(Calendar.MINUTE, 30);
               String time2 = df.format(cal.getTime());
      
               System.out.println("time1: "+time1+", time2: "+time2);
               List<Object> objs = dbSingleton.db.getData("Appointment", "apptdate='"+date+"' AND appttime>'"+time1+"' AND appttime<'"+time2+"'");
               System.out.println(objs.toString());
               if(objs.isEmpty()){
                  return true;
               } else {
                  return false;
               }
            } else {
               return false;
              }
         } else {
               return false;
           }
       } catch (Exception e) {
         e.printStackTrace();
         return false;
       }
     }
     
  // E.g. if the desired apt time is 10 AM, the isAppointmentAvailable fn checks if there is any apt b/w 9:45 - 10:15, if there's no apt, this fn is called,
  // this fn checks if there is any apt b/w 9:15 - 9:45 as already there's no apt b/w 9:45 and 10:00, further, if an apt exists in that period, it checks
  // if the phlebotomist for any of those found apts is similar to the one requested and if the pscs are different for those similar phlebotomists,
  // if that, it returns false as the phlebotomist would need 30 min to travel after finishing that appointment (15 min). 
     public boolean isPhlebotomistAvailable(String date, String time, String phlebotomistId, String pscId){
       try{
            String tempTime = time;
            // dbSingleton = DBSingleton.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            Date d = df.parse(tempTime); 
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.add(Calendar.MINUTE, -45);
            String time1 = df.format(cal.getTime());
            
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(d);
            cal.add(Calendar.MINUTE, 90);
            String time2 = df.format(cal.getTime());
   
            System.out.println("time: "+time2);
            System.out.println("time: "+time1);
            List<Object> objs = dbSingleton.db.getData("Appointment", "apptdate='"+date+"' AND appttime>'"+time1+"' AND appttime<'"+time2+"'");
            //System.out.println(objs.toString());
            List<Appointment> appts = new ArrayList<Appointment>();
            if(objs.isEmpty()){
               return true;
            } else {
               for (Object obj : objs){
                  Appointment appt = (Appointment) obj;
                  appts.add(appt);
               }
               for (Appointment appointment : appts) {
                  //System.out.println("Phl id: "+appointment.getPhlebid().getId());
                  if((appointment.getPhlebid().getId()).equals(phlebotomistId) && !(appointment.getPscid().getId()).equals(pscId)){
                        //System.out.println("PSC id: "+appointment.getPscid().getId());
                        //System.out.println("Different psc and same phlebotomist found");
                        return false;
                  }
               }
            }
       } catch (Exception e) {
         return false;
       }
       return true;
     }
     
     public boolean isAppointmentDuplicate(String date, String time, String patientId){
         System.out.println("method called");
        //  initialize();
         List<Object> objs = dbSingleton.db.getData("Appointment", "apptdate='"+date+"' AND appttime='"+time+"' AND patientid='"+patientId+"'");
         if(objs.isEmpty()){
            return false;
         } else {
            return true;
         }
     }
     
     public boolean isAppointmentTimeInRange(String time){
      try {
          String string1 = "07:59:59";
          Date time1 = new SimpleDateFormat("HH:mm:ss").parse(string1);
          Calendar calendar1 = Calendar.getInstance();
          calendar1.setTime(time1);
      
          String string2 = "17:00:01";
          Date time2 = new SimpleDateFormat("HH:mm:ss").parse(string2);
          Calendar calendar2 = Calendar.getInstance();
          calendar2.setTime(time2);
      
          String someRandomTime = time;
          Date d = new SimpleDateFormat("HH:mm:ss").parse(time);
          Calendar calendar3 = Calendar.getInstance();
          calendar3.setTime(d);
      
          Date x = calendar3.getTime();
          if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
              //checkes whether the current time is between 08:00:00 and 17:00:00.
              return true;
          } else {
              return false;
          }
      } catch (ParseException e) {
          e.printStackTrace();
          return false;
      }
   }

}