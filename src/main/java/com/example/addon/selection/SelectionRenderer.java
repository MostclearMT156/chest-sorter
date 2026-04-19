//package com.example.addon.selection;
//
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.render.*;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Box;
//import net.minecraft.util.math.Vec3d;
//
///**
// * Отвечает за визуальное отображение выделенной области.
// * Рендерит:
// *  - рамку выделения (startPos → endPos)
// *  - подсветку блока под курсором (если нужно)
// *
// * Используется в HUD или WorldRenderEvent.
// */
//public class SelectionRenderer {
//
//    private final SelectionManager selectionManager;
//
//    public SelectionRenderer(SelectionManager selectionManager) {
//        this.selectionManager = selectionManager;
//    }
//
//    /**
//     * Основной метод рендера.
//     * Вызывается каждый кадр в world-render фазе.
//     */
//    public void render(MatrixStack matrices, float tickDelta) {
//        if (!selectionManager.hasSelection()) return;
//
//        BlockPos start = selectionManager.getStartPos();
//        BlockPos end = selectionManager.getEndPos();
//
//        if (start == null || end == null) return;
//
//        Box box = createBox(start, end);
//        drawBox(matrices, box, 0.2f, 0.6f, 1.0f, 0.6f); // голубая рамка
//    }
//
//    /**
//     * Создаёт AABB-бокс между двумя точками.
//     */
//    private Box createBox(BlockPos a, BlockPos b) {
//        int x1 = Math.min(a.getX(), b.getX());
//        int y1 = Math.min(a.getY(), b.getY());
//        int z1 = Math.min(a.getZ(), b.getZ());
//
//        int x2 = Math.max(a.getX(), b.getX()) + 1;
//        int y2 = Math.max(a.getY(), b.getY()) + 1;
//        int z2 = Math.max(a.getZ(), b.getZ()) + 1;
//
//        return new Box(x1, y1, z1, x2, y2, z2);
//    }
//
//    /**
//     * Рисует прозрачную рамку вокруг выделенной области.
//     */
//    private void drawBox(MatrixStack matrices, Box box, float r, float g, float b, float alpha) {
//        MinecraftClient mc = MinecraftClient.getInstance();
//        Camera camera = mc.gameRenderer.getCamera();
//        Vec3d camPos = camera.getPos();
//
//        Box shifted = box.offset(-camPos.x, -camPos.y, -camPos.z);
//
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.disableTexture();
//
//        RenderSystem.setShader(GameRenderer::getPositionColorShader);
//
//        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
//        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);
//
//        // 12 рёбер бокса
//        line(buffer, shifted.minX, shifted.minY, shifted.minZ, shifted.maxX, shifted.minY, shifted.minZ, r, g, b, alpha);
//        line(buffer, shifted.minX, shifted.minY, shifted.minZ, shifted.minX, shifted.maxY, shifted.minZ, r, g, b, alpha);
//        line(buffer, shifted.minX, shifted.minY, shifted.minZ, shifted.minX, shifted.minY, shifted.maxZ, r, g, b, alpha);
//
//        line(buffer, shifted.maxX, shifted.maxY, shifted.maxZ, shifted.minX, shifted.maxY, shifted.maxZ, r, g, b, alpha);
//        line(buffer, shifted.maxX, shifted.maxY, shifted.maxZ, shifted.maxX, shifted.minY, shifted.maxZ, r, g, b, alpha);
//        line(buffer, shifted.maxX, shifted.maxY, shifted.maxZ, shifted.maxX, shifted.maxY, shifted.minZ, r, g, b, alpha);
//
//        line(buffer, shifted.minX, shifted.maxY, shifted.minZ, shifted.minX, shifted.maxY, shifted.maxZ, r, g, b, alpha);
//        line(buffer, shifted.minX, shifted.maxY, shifted.minZ, shifted.maxX, shifted.maxY, shifted.minZ, r, g, b, alpha);
//
//        line(buffer, shifted.maxX, shifted.minY, shifted.minZ, shifted.maxX, shifted.minY, shifted.maxZ, r, g, b, alpha);
//        line(buffer, shifted.maxX, shifted.minY, shifted.minZ, shifted.maxX, shifted.maxY, shifted.minZ, r, g, b, alpha);
//
//        line(buffer, shifted.minX, shifted.minY, shifted.maxZ, shifted.maxX, shifted.minY, shifted.maxZ, r, g, b, alpha);
//        line(buffer, shifted.minX, shifted.minY, shifted.maxZ, shifted.minX, shifted.maxY, shifted.maxZ, r, g, b, alpha);
//
//        Tessellator.getInstance().draw();
//
//        RenderSystem.enableTexture();
//        RenderSystem.disableBlend();
//    }
//
//    /**
//     * Добавляет линию в буфер.
//     */
//    private void line(BufferBuilder buffer,
//                      double x1, double y1, double z1,
//                      double x2, double y2, double z2,
//                      float r, float g, float b, float a) {
//
//        buffer.vertex(x1, y1, z1).color(r, g, b, a).next();
//        buffer.vertex(x2, y2, z2).color(r, g, b, a).next();
//    }
//}
