package org.ut01.dao;

import generated.Articulo;
import generated.Articulos;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ut01.GestorEventos;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

public class billDAOImpl implements BillDAO {

    /**
     * @param f Variable file creada a partir del path del XML
     * @return Devuelve los datos de tipo Articulos despues de extraerlos
     */
    @Override
    public Articulos extractInfo(File f) {
        try {
            JAXBContext contextoArticulos = JAXBContext.newInstance(Articulos.class);
            Unmarshaller unmarshaller = contextoArticulos.createUnmarshaller();
            unmarshaller.setSchema(
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File("src/main/resources/xsd/invoice_202009.xsd")));
            unmarshaller.setEventHandler(new GestorEventos());
            return (Articulos) unmarshaller.unmarshal(f);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param articulos Informacion de los articulos de los cuales escribiremos los resultados en el .txt
     * @param file      Archivo creado en el que escribiremos los resultados
     * @param pathXml   Ruta a la factura original para sacar la informacion solicitada en el archivo
     */
    @Override
    public void writeBill(Articulos articulos, File file, String pathXml) {
        try {
            File f = new File(pathXml);
            FileWriter fileWriter = new FileWriter(file);
            String content = printResultBill(articulos, f);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param articulos Datos de los articulos para incluirlos en el excel/workbook
     * @param file Archivo file donde cre oe lexcel y luego escribo en el los datos y la estructura
     */
    @Override
    public void createWorkbook(Articulos articulos, File file) {
        try (Workbook workbook = new XSSFWorkbook()) {
            int i = workbook.getNumberOfSheets();
            Sheet sheet = workbook.createSheet("Factura " + i + 1);
            createHeader(sheet, workbook);
            createBody(articulos, sheet, workbook);
            createFile(workbook, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Crea el archivo y escribe el contenido
    private void createFile(Workbook workbook, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
            System.out.println("Excel Factura Creado");
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Metodo para crear el cuerpo del excel con su contenido extraido por cada articulo
    private void createBody(Articulos articulos, Sheet sheet, Workbook workbook) {
        for (int i = 0; i < articulos.getArticulo().size(); i++) {
            BigDecimal coste = articulos.getArticulo().get(i).getCostes().getProduccion().add(articulos.getArticulo().get(i).getCostes().getDerivados().add(articulos.getArticulo().get(i).getImpuestos()));

            Row row = sheet.createRow(i + 1);

            // Obtener el estilo con color y el estilo de borde
            CellStyle estiloColor = setColorForegroundByRow(workbook, i);
            CellStyle estiloBordes = setBorderColor(workbook);
            CellStyle estiloHuecos = setFormatStyle(workbook);

            // Combinar estilos de color , bordes y formato (Al aplicar muchos estilos terminan dejando
            // de funcionar , por lo que he decidido juntarlos)
            CellStyle estiloCompleto = combineStyles(workbook, estiloColor, estiloBordes, estiloHuecos);

            // Crear las celdas de la fila y aplicar el estilo (Extraigo tod0 el codigo a un metodo para
            // que quede mas ordenado y claro)
            createCell(row, 0, articulos.getArticulo().get(i).getNombre(), estiloCompleto);
            createCell(row, 1, articulos.getArticulo().get(i).getTipo(), estiloCompleto);
            createCell(row, 2, articulos.getArticulo().get(i).getFechaVenta(), estiloCompleto);
            createCell(row, 3, articulos.getArticulo().get(i).getPrecio().getValue().toString(), estiloCompleto);
            createCell(row, 4, articulos.getArticulo().get(i).getCostes().getDerivados().toString(), estiloCompleto);
            createCell(row, 5, articulos.getArticulo().get(i).getCostes().getProduccion().toString(), estiloCompleto);
            createCell(row, 6, articulos.getArticulo().get(i).getImpuestos().toString(), estiloCompleto);
            createCell(row, 7, articulos.getArticulo().get(i).getPrecio().getValue().subtract(coste).toString(), estiloCompleto);
        }
    }

    // Metodo que crea las celdas con los datos extraidos y con su estilo
    private void createCell(Row row, int Index, String value, CellStyle style) {
        Cell cell = row.createCell(Index);

        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    // Metodo que permite ver el contenido completo de la celda y ademas se coloca para una vista mas clara
    // (Arriba izquierda) tambien salta de linea (con esto es con lo que vemos el contenido conmpleto)
    private CellStyle setFormatStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setWrapText(true);

        return cellStyle;
    }

    // Metodo para poner los bordes al excel con estilos
    private CellStyle setBorderColor(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();

        cellStyle.setBorderBottom(BorderStyle.MEDIUM);
        cellStyle.setBorderTop(BorderStyle.MEDIUM);
        cellStyle.setBorderLeft(BorderStyle.MEDIUM);
        cellStyle.setBorderRight(BorderStyle.MEDIUM);

        cellStyle.setBottomBorderColor(IndexedColors.GREEN.getIndex());
        cellStyle.setTopBorderColor(IndexedColors.GREEN.getIndex());
        cellStyle.setLeftBorderColor(IndexedColors.GREEN.getIndex());
        cellStyle.setRightBorderColor(IndexedColors.GREEN.getIndex());

        return cellStyle;
    }

    // Metodo para filtrar y colorear el fondo segun la fila que sea , siendo par o impar
    // (una linea de un color y la siguiente de otro)
    private CellStyle setColorForegroundByRow(Workbook workbook, int index) {
        CellStyle cellStyle = workbook.createCellStyle();
        if (index % 2 == 0) {
            cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        } else {
            cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        }
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    }

    // Metodo que combina los 3 estilos (color de fondo y bordes ademas del formato)
    private CellStyle combineStyles(Workbook workbook, CellStyle colorStyle, CellStyle borderStyle, CellStyle estiloHuecos) {
        CellStyle combinedStyle = workbook.createCellStyle();
        combinedStyle.cloneStyleFrom(colorStyle);
        combinedStyle.setBorderBottom(borderStyle.getBorderBottom());
        combinedStyle.setBorderTop(borderStyle.getBorderTop());
        combinedStyle.setBorderLeft(borderStyle.getBorderLeft());
        combinedStyle.setBorderRight(borderStyle.getBorderRight());
        combinedStyle.setBottomBorderColor(borderStyle.getBottomBorderColor());
        combinedStyle.setTopBorderColor(borderStyle.getTopBorderColor());
        combinedStyle.setLeftBorderColor(borderStyle.getLeftBorderColor());
        combinedStyle.setRightBorderColor(borderStyle.getRightBorderColor());
        combinedStyle.setVerticalAlignment(estiloHuecos.getVerticalAlignment());
        combinedStyle.setAlignment(estiloHuecos.getAlignment());
        combinedStyle.setWrapText(estiloHuecos.getWrapText());
        return combinedStyle;
    }

    // Metodo de creacion de las celdas de arriba , header (En este caso no uso el primer combinado
    // porque no usan el filtro por lineas , creo otro combinado)
    private void createHeader(Sheet sheet, Workbook workbook) {
        CellStyle estiloBordes = setBorderColor(workbook);
        CellStyle estiloHuecos = setFormatStyle(workbook);

        CellStyle estiloHeader = combineStylesH(workbook, estiloBordes, estiloHuecos);

        // Aqui es como en el body , extraigo a un metodo y queda mas claro , aplicando los estilos que
        // necesito
        Row row = sheet.createRow(0);
        createHeaderCell(row, 0, "Articulo", estiloHeader);
        createHeaderCell(row, 1, "Tipo", estiloHeader);
        createHeaderCell(row, 2, "Fecha de venta", estiloHeader);
        createHeaderCell(row, 3, "Precio venta", estiloHeader);
        createHeaderCell(row, 4, "Costes derivados", estiloHeader);
        createHeaderCell(row, 5, "Costes producción", estiloHeader);
        createHeaderCell(row, 6, "Impuestos", estiloHeader);
        createHeaderCell(row, 7, "Beneficio", estiloHeader);
    }

    // Metodo que junta todos los styles requeridos en el Header
    private CellStyle combineStylesH(Workbook workbook, CellStyle estiloBordes, CellStyle estiloHuecos) {
        CellStyle combinedStyle = workbook.createCellStyle();
        combinedStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        combinedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        combinedStyle.setBorderBottom(estiloBordes.getBorderBottom());
        combinedStyle.setBorderTop(estiloBordes.getBorderTop());
        combinedStyle.setBorderLeft(estiloBordes.getBorderLeft());
        combinedStyle.setBorderRight(estiloBordes.getBorderRight());
        combinedStyle.setBottomBorderColor(estiloBordes.getBottomBorderColor());
        combinedStyle.setTopBorderColor(estiloBordes.getTopBorderColor());
        combinedStyle.setLeftBorderColor(estiloBordes.getLeftBorderColor());
        combinedStyle.setRightBorderColor(estiloBordes.getRightBorderColor());
        combinedStyle.setVerticalAlignment(estiloHuecos.getVerticalAlignment());
        combinedStyle.setAlignment(estiloHuecos.getAlignment());
        combinedStyle.setWrapText(estiloHuecos.getWrapText());
        Font font = workbook.createFont();
        font.setBold(true);
        combinedStyle.setFont(font);
        return combinedStyle;
    }

    // Crea el header con las celdas que necesitamos , el texto y el estilo completo
    private void createHeaderCell(Row row, int columnIndex, String value, CellStyle combinedStyle) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellStyle(combinedStyle);
        cell.setCellValue(value);
    }


    // Metodo que imprime por pantalla el resultado de la factura con los datos necesitados y el nombre
    // requerido
    private String printResultBill(Articulos articulos, File f) {
        String result;
        File excelFile = new File(f.getParent() + "/bill.xlsx");
        String[] name = f.getName().split("\\.");
        result = "Factura: " + name[0] + "\n" +
                "NumeroDeArticulos: " + articulos.getArticulo().size() + "\n" +
                "BeneficioTotal: " + getBeneficioTotal(articulos) + "\n" +
                "Ruta del fichero: " + f.getPath() + "\n" +
                "Nombre del fichero: " + f.getName() + "\n" +
                "Tamaño del fichero: " + f.length() + "\n" +
                "Ruta Excel: " + excelFile.getPath() + "\n" +
                "Tamaño Excel: " + excelFile.length();

        return result;
    }

    // Metodo que calcula el beneficio total de todos los articulos
    private BigDecimal getBeneficioTotal(Articulos articulos) {
        BigDecimal contador = BigDecimal.valueOf(0);
        for (Articulo articulo : articulos.getArticulo()) {
            BigDecimal contador2;
            BigDecimal coste = articulo.getCostes().getProduccion().add(articulo.getCostes().getDerivados().add(articulo.getImpuestos()));
            contador2 = (articulo.getPrecio().getValue().subtract(coste));
            contador = contador.add(contador2);
        }
        return contador;
    }
}
