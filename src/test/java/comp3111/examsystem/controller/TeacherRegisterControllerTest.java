package comp3111.examsystem.controller;

import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import comp3111.examsystem.data.Position;
import comp3111.examsystem.entity.Teacher;
import comp3111.examsystem.tools.MsgSender;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

public class TeacherRegisterControllerTest {
    private TeacherRegisterController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new TeacherRegisterController();
        setField(controller, "usernameTxt", new TextField());
        setField(controller, "nameTxt", new TextField());
        setField(controller, "ageTxt", new TextField());
        setField(controller, "passwordTxt", new PasswordField());
        setField(controller, "passwordConfirmTxt", new PasswordField());
        setField(controller, "genderCmb", new ComboBox<>());
        setField(controller, "departmentCmb", new ComboBox<>());
        setField(controller, "positionCmb", new ComboBox<>());
        ((ComboBox<Gender>) getField(controller, "genderCmb")).getItems().setAll(Gender.values());
        ((ComboBox<Department>) getField(controller, "departmentCmb")).getItems().setAll(Department.values());
        ((ComboBox<Position>) getField(controller, "positionCmb")).getItems().setAll(Position.values());
        ((ComboBox<Gender>) getField(controller, "genderCmb")).setValue(Gender.MALE);
        ((ComboBox<Department>) getField(controller, "departmentCmb")).setValue(Department.CSE);
        ((ComboBox<Position>) getField(controller, "positionCmb")).setValue(Position.P);
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    private Object getField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    @Test
    void testRegisterWithValidFields() throws Exception {
        String uniqueUsername = "teacher1_" + System.currentTimeMillis();
        ((TextField) getField(controller, "usernameTxt")).setText(uniqueUsername);
        ((TextField) getField(controller, "nameTxt")).setText("Alice");
        ((TextField) getField(controller, "ageTxt")).setText("30");
        ((PasswordField) getField(controller, "passwordTxt")).setText("pass123");
        ((PasswordField) getField(controller, "passwordConfirmTxt")).setText("pass123");
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            msgSenderMocked.when(() -> MsgSender.showMsg(Mockito.anyString()))
                .thenAnswer(invocation -> {
                    System.out.println("MsgSender.showMsg called with: " + invocation.getArgument(0));
                    return null;
                });
            var register = controller.getClass().getDeclaredMethod("register", javafx.event.ActionEvent.class);
            register.setAccessible(true);
            register.invoke(controller, (Object) null);
            msgSenderMocked.verify(() -> MsgSender.showMsg(Mockito.anyString()));
        }
    }

    @Test
    void testRegisterWithMissingFields() throws Exception {
        ((TextField) getField(controller, "usernameTxt")).setText("");
        ((TextField) getField(controller, "nameTxt")).setText("");
        ((TextField) getField(controller, "ageTxt")).setText("");
        ((PasswordField) getField(controller, "passwordTxt")).setText("");
        ((PasswordField) getField(controller, "passwordConfirmTxt")).setText("");
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            var register = controller.getClass().getDeclaredMethod("register", javafx.event.ActionEvent.class);
            register.setAccessible(true);
            register.invoke(controller, (Object) null);
            msgSenderMocked.verify(() -> MsgSender.showMsg(Mockito.contains("Please fill in all fields")));
        }
    }

    @Test
    void testRegisterWithMismatchedPasswords() throws Exception {
        ((TextField) getField(controller, "usernameTxt")).setText("teacher2");
        ((TextField) getField(controller, "nameTxt")).setText("Bob");
        ((TextField) getField(controller, "ageTxt")).setText("40");
        ((PasswordField) getField(controller, "passwordTxt")).setText("pass123");
        ((PasswordField) getField(controller, "passwordConfirmTxt")).setText("pass456");
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            var register = controller.getClass().getDeclaredMethod("register", javafx.event.ActionEvent.class);
            register.setAccessible(true);
            register.invoke(controller, (Object) null);
            msgSenderMocked.verify(() -> MsgSender.showMsg(Mockito.contains("Password and Confirm Password do not match")));
        }
    }
} 