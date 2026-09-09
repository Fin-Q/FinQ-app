package com.swyp.FinQ.global.contract;

import io.swagger.v3.oas.annotations.Operation;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OperationIdContractTest {

    private static final String BASE_PACKAGE = "com.swyp.FinQ";
    private static final String BASE_PATH = "src/main/java/com/swyp/FinQ";

    /** operationId 적용 대상 도메인 패키지 */
    private static final Set<String> TARGET_PACKAGES = Set.of(
            "com.swyp.FinQ.content.controller",
            "com.swyp.FinQ.learning.controller",
            "com.swyp.FinQ.home.controller",
            "com.swyp.FinQ.reward.controller"
    );

    @Test
    void targetControllerMethodsHaveUniqueOperationId() {
        List<Class<?>> controllers = findControllerClasses();
        List<Class<?>> targets = controllers.stream()
                .filter(c -> TARGET_PACKAGES.contains(c.getPackageName()))
                .toList();
        assertThat(targets).as("대상 컨트롤러를 찾지 못했습니다").isNotEmpty();

        Map<String, String> operationIdToMethod = new HashMap<>();
        List<String> missing = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();

        for (Class<?> controller : targets) {
            for (Method method : controller.getDeclaredMethods()) {
                Operation op = method.getAnnotation(Operation.class);
                String label = controller.getSimpleName() + "#" + method.getName();

                if (op == null || op.operationId().isEmpty()) {
                    missing.add(label);
                    continue;
                }

                String prev = operationIdToMethod.put(op.operationId(), label);
                if (prev != null) {
                    duplicates.add(op.operationId() + " → [" + prev + ", " + label + "]");
                }
            }
        }

        assertThat(missing)
                .as("operationId가 누락된 메서드")
                .isEmpty();
        assertThat(duplicates)
                .as("중복된 operationId")
                .isEmpty();
    }

    @Test
    void operationIdIsNotDuplicatedAcrossAllControllers() {
        List<Class<?>> controllers = findControllerClasses();

        Map<String, String> operationIdToMethod = new HashMap<>();
        List<String> duplicates = new ArrayList<>();

        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                Operation op = method.getAnnotation(Operation.class);
                if (op == null || op.operationId().isEmpty()) continue;

                String label = controller.getSimpleName() + "#" + method.getName();
                String prev = operationIdToMethod.put(op.operationId(), label);
                if (prev != null) {
                    duplicates.add(op.operationId() + " → [" + prev + ", " + label + "]");
                }
            }
        }

        assertThat(duplicates)
                .as("전체 컨트롤러에서 중복된 operationId")
                .isEmpty();
    }

    private List<Class<?>> findControllerClasses() {
        List<Class<?>> result = new ArrayList<>();
        File baseDir = new File(BASE_PATH);
        scanDirectory(baseDir, BASE_PACKAGE, result);
        return result;
    }

    private void scanDirectory(File dir, String pkg, List<Class<?>> result) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, pkg + "." + file.getName(), result);
            } else if (file.getName().endsWith("Controller.java")) {
                String className = pkg + "." + file.getName().replace(".java", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(RestController.class)
                            || clazz.isAnnotationPresent(Controller.class)) {
                        result.add(clazz);
                    }
                } catch (ClassNotFoundException ignored) {
                }
            }
        }
    }
}