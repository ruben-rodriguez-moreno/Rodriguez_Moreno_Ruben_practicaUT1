package org.ut01.dao;

import generated.Articulos;

import java.io.File;

public interface BillDAO {
    /**
     * @param f Variable file creada a partir del path del XML
     * @return Devuelve los datos de tipo Articulos despues de extraerlos
     */
    Articulos extractInfo(File f);

    /**
     * @param articulos Articulos de la factura para escribir sus datos
     * @param file      Archivo creado para escribir
     * @param pathXml   Ruta factura original para introducir informacion del archivo
     */
    void writeBill(Articulos articulos, File file, String pathXml);

    /**
     * @param articulos Informacion de los articulos para añadirlos al excel
     * @param file Archivo file con el que creo el excel y escribo el contenido (El file incluye la ruta)
     */
    void createWorkbook(Articulos articulos, File file);
}
