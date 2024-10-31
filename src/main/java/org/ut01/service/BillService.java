package org.ut01.service;

import generated.Articulo;
import generated.Articulos;
import org.ut01.dao.BillDAO;
import org.ut01.dao.billDAOImpl;

import java.io.File;
import java.math.BigDecimal;

public class BillService {
    BillDAO billDAO = new billDAOImpl();

    /**
     * @param pathXml Ruta al archivo XML con los datos de la factura (bill)
     */
    public void getBill(String pathXml) {
        //Creamos un file con la ruta , extraemos los datos y los metemos en el metodo printBill
        //para hacer la informacion graficamente accesible
        File f = new File(pathXml);
        Articulos articulos = billDAO.extractInfo(f);
        printBill(articulos);
    }

    /**
     * @param articulos Informacion extraida de los articulos
     */
    private void printBill(Articulos articulos) {
        //Mediante un for each se imprime la informacion de cada factura
        for (Articulo articulo : articulos.getArticulo()) {
            System.out.println("=======================================");
            BigDecimal coste = articulo.getCostes().getProduccion().add(articulo.getCostes().getDerivados().add(articulo.getImpuestos()));
            System.out.println("Articulo: " + articulo.getNombre());
            System.out.println("Tipo: " + articulo.getTipo());
            System.out.println("Precio de venta: " + articulo.getPrecio().getValue());
            System.out.println("Moneda: " + articulo.getPrecio().getMoneda());
            System.out.println("Coste: " + coste);
            System.out.println("Beneficio: " + articulo.getPrecio().getValue().subtract(coste));
        }
    }

    /**
     * @param path    Ruta donde se va a crear el .txt de la factura
     * @param pathXml Otra ruta de la factura .xml con la cual sacaremos informacion del archivo
     */
    public void newResultBill(String path, String pathXml) {
        File f = new File(pathXml);
        String[] name = f.getName().split("\\.");
        Articulos articulos = billDAO.extractInfo(f);
        File file = new File(path + "result_" + name[0] + ".txt");
        billDAO.writeBill(articulos, file, pathXml);

    }


    /**
     * @param pathExcel Ruta donde se creeara el archivo excel
     * @param pathXml Ruta donde extraigo la informacion de la factura
     */
    public void createExcel(String pathExcel, String pathXml) {
        File f = new File(pathXml);
        Articulos articulos = billDAO.extractInfo(f);
        billDAO.createWorkbook(articulos, new File(pathExcel));
    }
}
