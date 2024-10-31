package org.ut01;

import org.ut01.service.BillService;

public class Main {
    private static final String PATH_XML = "src/main/resources/invoice_202009.xml";

    public static void main(String[] args) {
        BillService billService = new BillService();
        //Coger los datos del XML y sacar la factura
        billService.getBill(PATH_XML);
    }
}