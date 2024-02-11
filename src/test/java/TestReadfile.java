import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;

public class TestReadfile {


  @Test
  void name() throws IOException {
    File file = ResourceUtils.getFile("classpath:start.txt");

    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

    while (bufferedReader.ready()) {
      String s = bufferedReader.readLine();
      System.out.println(s);
    }

  }
}
