package net.tospay.transaction;

import java.util.UUID;
import net.tospay.transaction.controllers.FetchController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    @Autowired
    private FetchController controller;

    @Test
    public void contexLoads() throws Exception {
        assertThat(controller).isNotNull();
    }


}
