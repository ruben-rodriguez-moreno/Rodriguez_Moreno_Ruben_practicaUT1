package org.ut01;

import org.ut01.service.BillService;

public class Main3 {
    //Ruta donde se va a crear el fichero EXCEL
    private static final String PATH_EXCEL = "src/main/resources/bill.xlsx";
    //Ruta XML donde saco la informacion
    private static final String PATH_XML = "src/main/resources/invoice_202009.xml";

    public static void main(String[] args) {
        //Llamada al servicio
        BillService billService = new BillService();
        //Utilizacion del servicio para el metodo que crea el excel con los datos del XML
        billService.createExcel(PATH_EXCEL, PATH_XML);
    }
}
