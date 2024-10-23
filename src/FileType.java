package utility;

import jakarta.servlet.http.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileType {
    private File file;
    private String fileExtension;

    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }
    
    public String getFileExtension(){
        return this.fileExtension;
    }

    public String getFileExtension(String fileName){
        String[] split = fileName.split("\\.");
        return split.length > 1 ? "."+split[split.length - 1] : "";
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public void setFile(Part part) throws Exception {
        String fileName = part.getSubmittedFileName();
        this.setFileExtension(this.getFileExtension(fileName));

        File tempFile = File.createTempFile(fileName, null);

        try (InputStream input = part.getInputStream();
             FileOutputStream output = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
        this.setFile(tempFile);
    }
    
     /**
     * Deplace le fichier temporaire vers le repertoire de destination avec un nouveau nom.
     * 
     * @param uploadPath   Le chemin vers lequel le fichier sera deplacé.
     * @param newFileName 
     */
    public void upload(String uploadPath, String newFileName)throws Exception{
        String extension = this.getFileExtension();
        Path destination = new File(uploadPath, newFileName+extension).toPath();
        Files.move(this.getFile().toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
    }

    public FileType(Part part) throws Exception {
        this.setFile(part);
    }
}
