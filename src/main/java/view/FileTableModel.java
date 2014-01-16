package view;

import java.io.File;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;


/**
 * Class that extends AbstractTableModel for localTable in {@link view.ClientMainFrame}
 * 
 * @author Jakub Fortunka
 *
 */
class FileTableModel extends AbstractTableModel {

	/**
	 * need in case of serializing object
	 */
	private static final long serialVersionUID = 7436046220038792495L;
	
	/**
	 * representation of empty file (is used for showing ".." in table)
	 */
	private File emptyFile=null;
    /**
     * array of files which will be written out in table
     */
    private File[] files;
    /**
     * current fileSystemView (for file icons)
     */
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    /**
     * columns of table
     */
    private String[] columns = {
        "Icon",
        "File",
        "Size",
        "Last Modified",
        "R",
        "W",
        "E",
        "D",
        "F",
    };

    FileTableModel() {
        this(new File[0]);
    }

    FileTableModel(File[] files) {
        this.files = files;
    }

    /** 
     * method that is responsible for appropriate showing content of columns
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int column) {
    	File file;
    	if (row<1) file=emptyFile;
    	else file = files[row-1];
        switch (column) {
            case 0:
            	return fileSystemView.getSystemIcon(file);
            case 1:
            	if (row==0) return "..";
            	else return fileSystemView.getSystemDisplayName(file);
            //	return fileSystemView.getSystemDisplayName(file);
            case 2:
            	if (row==0) return null;
            	else return file.length();
            //	return file.length();
            case 3:
            	if (row==0) return null;
            	else return file.lastModified();
            //	return file.lastModified();
            case 4:
            	if (row==0) return null;
            	else return file.canRead();
            //	return file.canRead();
            case 5:
            	if (row==0) return null;
            	else return file.canWrite();
            //	return file.canWrite();
            case 6:
            	if (row==0) return null;
            	else return file.canExecute();
            //	return file.canExecute();
            case 7:
            	if (row==0) return null;
            	else return file.isDirectory();
            //	return file.isDirectory();
            case 8:
            	if (row==0) return null;
            	else return file.isFile();
            //	return file.isFile();
            default:
                System.err.println("Logic Error");
        }
        return "";
    }

    /** 
     * gets number of columns
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return columns.length;
    }

    /** 
     * gets Class of column (ImageIcon, Long, Date, Boolean and String)
     * 
     * @param column number of column which class we want to get
     * 
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return ImageIcon.class;
            case 2:
                return Long.class;
            case 3:
                return Date.class;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            	return Boolean.class;          
        }
        return String.class;
    }

    /**
     * gets name of column which number is passed
     * 
     * @param column number of column which name we want to get
     * 
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName(int column) {
        return columns[column];
    }

    /** 
     * gets number of rows in table
     * 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return files.length+1;
    }

    /**
     * method gets file at passed row
     * 
     * @param row number of row for which we want to get file
     * @return
     */
    public File getFile(int row) {
    	if (row>1)	return files[row-1];
    	return emptyFile;
    //	return files[row];
    }

    /**
     * sets private field {@link view.FileTableModel#files} with passed argument
     * 
     * @param files list of Files in directory
     */
    public void setFiles(File[] files) {
        this.files = files;
        fireTableDataChanged();
    }
}