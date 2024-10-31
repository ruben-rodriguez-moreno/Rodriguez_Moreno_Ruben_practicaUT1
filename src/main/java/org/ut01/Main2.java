package org.ut01;

import org.ut01.service.BillService;

public class Main2 {
    //Ruta de la factura
    private static final String PATH_XML = "src/main/resources/invoice_202009.xml";
    //Ruta para crear el fichero
    private static final String PATH = "src/main/resources/";

    public static void main(String[] args) {
        BillService billService = new BillService();
        //Coger los datos del XML y sacar la factura
        billService.getBill(PATH_XML);
        //Creamos el archivo con los resultados
        billService.newResultBill(PATH, PATH_XML);
    }
}