package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Jeffery
 * @version 1.0
 */
public class PrescriptionsAll {

    private static Map prescriptionAll = new HashMap();


    public static void addPrescription(Prescription prescription){
    	prescriptionAll.put(new Integer(prescription.getId()),prescription);
    }

    public static Prescription getPrescription(int id){
        return (Prescription)prescriptionAll.get(new Integer(id));
    }


}
