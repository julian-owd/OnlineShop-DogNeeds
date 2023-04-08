package de.dogneeds.backend.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Random;

@Service
public class FirebaseStorageService {

    /**
     * upload a file to firebase
     *
     * @param file the file
     * @return the name of the file how it's called in firebase
     */
    public String save(MultipartFile file) throws IOException {
        String name = generateRandomName(file.getOriginalFilename());

        Bucket bucket = StorageClient.getInstance().bucket();
        bucket.create("product_images/" + name, file.getBytes(), file.getContentType());
        System.out.println("Uploaded picture " + name);

        return name;
    }

    /**
     * loads a file from firebase
     *
     * @param file the name of the file
     * @return byte array of the file or null if not found
     */
    public byte[] load(String file) {
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get("product_images/" + file);
        if (blob != null) {
            return blob.getContent();
        }
        return null;
    }

    /**
     * delete a file from firebase
     *
     * @param file the name of the file
     * @return true or false whether the action was successful
     */
    public boolean delete(String file) {
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get("product_images/" + file);
        if (blob != null) {
            System.out.println("Deleting picture " + file);
            return blob.delete();
        }
        return false;
    }

    /**
     * generate a random prefix for a file
     *
     * @param originalName the original name of the file
     * @return the random name ({random}-{original}.{ending})
     */
    private String generateRandomName(String originalName) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int nameLength = 8;
        Random random = new Random();
        StringBuilder builder = new StringBuilder(nameLength);
        for (int i = 0; i < nameLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            builder.append((char) randomLimitedInt);
        }

        builder.append("-");
        builder.append(originalName);

        return builder.toString();
    }

}
