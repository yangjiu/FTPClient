package view;

import java.io.File;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;

/** A TableModel to hold File[]. */
class FileTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7436046220038792495L;
	
	private File emptyFile=null;
    private File[] files;
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
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
            case 2:
            	if (row==0) return null;
            	else return file.length();
            case 3:
            	if (row==0) return null;
            	else return file.lastModified();
            case 4:
            	if (row==0) return null;
            	else return file.canRead();
            case 5:
            	if (row==0) return null;
            	else return file.canWrite();
            case 6:
            	if (row==0) return null;
            	else return file.canExecute();
            case 7:
            	if (row==0) return null;
            	else return file.isDirectory();
            case 8:
            	if (row==0) return null;
            	else return file.isFile();
            default:
                System.err.println("Logic Error");
        }
        return "";
    }

    public int getColumnCount() {
        return columns.length;
    }

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

    public String getColumnName(int column) {
        return columns[column];
    }

    public int getRowCount() {
        return files.length+1;
    }

    public File getFile(int row) {
    	if (row<1)	return files[row-1];
    	return emptyFile;
    }

    public void setFiles(File[] files) {
        this.files = files;
        fireTableDataChanged();
    }
}