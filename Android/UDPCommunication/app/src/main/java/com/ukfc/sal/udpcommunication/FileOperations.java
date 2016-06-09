package com.ukfc.sal.udpcommunication;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.PatternSyntaxException;

/**
 * Created by Sal on 17/01/2016.
 */
public class FileOperations{
    private Context context;
    private String path="";
    public FileOperations(Context context){
        this.context=context;
        path=context.getFilesDir().getAbsolutePath() + "/";
    }

    public FileOperations(){
        this.context=null;
    }

    public boolean checkFileExists(String filename){
        String filepath = path+filename;
        File file = new File(filepath);
        if(file.exists())
            return true;
        return false;
    }

    public boolean checkIfDuplicateData(String filename,String data){
        BufferedReader reader=null;
        String filepath = path+filename;
        try{
            reader = new BufferedReader(new FileReader(filepath));
            String line="";
            while((line=reader.readLine())!=null){
                if(line.equals(data)) {
                    reader.close();
                    return true;
                }
            }
            reader.close();
        }
        catch(FileNotFoundException notFoundError) {
            Log.e("NotFoundError", notFoundError.toString());
            System.exit(0);
        }
        catch(IOException readFileError){
            Log.e("ReadFileError", readFileError.toString());
            System.exit(0);
        }
        return false;
    }

    /*
     * This method gets a particular data at the specified index.
     */
    public String getDataAtIndex(String filename,int index){
        BufferedReader reader=null;
        String filepath = path+filename;
        try{
            reader = new BufferedReader(new FileReader(filepath));
            String line="";
            int counter=0;
            while((line=reader.readLine())!=null){
                counter++;
                if(counter==index){
                    reader.close();
                    return line;
                }
            }
            reader.close();
        }
        catch(FileNotFoundException notFoundError) {
            Log.e("NotFoundError", notFoundError.toString());
            System.exit(0);
        }
        catch(IOException readFileError){
            Log.e("ReadFileError", readFileError.toString());
            System.exit(0);
        }
        return "";
        //Occurs if the given index is out of bounds
    }
    /*
     * This method deletes a specified data from a specified file
     */
    public void deleteDataFromFile(String filename,String entryForDeletion){
        BufferedReader reader=null;
        String filepath = path+filename;
        LinkedList<String>list = new LinkedList<String>();
        boolean foundEntryInList = false;
        try{
            reader = new BufferedReader(new FileReader(filepath));
            String line="";
            while((line=reader.readLine())!=null){
                if(!line.equals(entryForDeletion)){
                    list.addFirst(line);
                }
                else{
                    foundEntryInList = true;
                }
            }
            reader.close();
        }
        catch(FileNotFoundException notFoundError) {
            Log.e("NotFoundError", notFoundError.toString());
            System.exit(0);
        }
        catch(IOException readFileError){
            Log.e("ReadFileError", readFileError.toString());
            System.exit(0);
        }


        if(foundEntryInList==true) {
            int indexedElementPos = -1;
            //Write the new list in a file that overwrites the previous one
            ListIterator iterator = list.listIterator();
            while (iterator.hasNext()) {
                if (indexedElementPos == -1)
                    writeDataToFile(filename, iterator.next().toString(), true);
                    //Write the first item into a new file that overwrites the previous one
                else
                    writeDataToFile(filename, iterator.next().toString(), false);
                //Write the current item into an existing file
                indexedElementPos++;
            }

            if(indexedElementPos==-1)
                context.deleteFile(filename);
            //IF the data found is the only element in the file, just delete the list
        }
    }

    /*
     * This method deletes a given file.
     */
    public void deleteFile(String filename){
        context.deleteFile(filename);
    }

    /*
     * This method writes a given SINGLE unformatted data to the required file
     */
    public void writeDataToFile(String filename,String unformattedData,boolean overwrite){
        String formattedData =unformattedData+"\r";
        //Add a carriage return and starts the next type at the start of the next line
        FileOutputStream outputStream;
        try{
            if(overwrite==false)
                outputStream = context.openFileOutput(filename, Context.MODE_APPEND);
            else
                outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(formattedData.getBytes());
            outputStream.close();
        }
        catch(Exception e){
            Log.e("FileWritingError",e.toString());
            System.exit(0);
        }
    }
}
