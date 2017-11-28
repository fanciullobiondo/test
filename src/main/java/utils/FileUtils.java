package utils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author nicolo.boschi
 */
public class FileUtils {

    public static List<String> readExistingDatabases(String path) throws IOException {
        List<String> list = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths
                .filter(Files::isRegularFile)
                .filter(f ->  !f.toFile().getName().equals("null"))
                .forEach(f -> list.add(f.toFile().getName()));
        }
        System.out.println("return " + list);
        return list;

    }
}
