package de.jwieditz.miseal.utilities.unwrap;

import java.util.LinkedList;
import java.util.Queue;

import de.jwieditz.miseal.PatchedData;
import de.jwieditz.miseal.Point;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

public class Unwrapper {

    private final double eps = 0.001;
    private final double maxOffset = PI / 2 - eps;

    public void unwrap(PatchedData.Patch patch, UnwrapType type) {
        if (type == null || patch == null || patch.containsSingularity()) {
            return;
        }

        switch (type) {
            case LINES:
                unwrapLines(patch);
                break;

            case SPIRALS:
                unwrapSpirals(patch);
                break;

            case DIAMOND:
                unwrapDiamond(patch);
                break;
        }
    }

    private void unwrapLines(PatchedData.Patch patch) {
        int padX = patch.getPadX();
        int padY = patch.getPadY();
        int fromX = patch.getFromX() - padX;
        int fromY = patch.getFromY() - padY;
        int toX = patch.getToX() + padX;
        int toY = patch.getToY() + padY;

        double[] offsets = new double[toY - fromY];

        int firstCol = fromX;
        int firstRow = fromY;

        while (firstCol < patch.getToX() && firstRow < toY && Double.isNaN(patch.get(PatchedData.DataType.ORIENTATION, firstCol, firstRow))) {
            firstCol++;
            firstRow++;
        }

        // determine offsets of the first column
        for (int row = firstRow + 1; row < toY; row++) {
            double current = patch.get(PatchedData.DataType.ORIENTATION, firstCol, row);
            double last = patch.get(PatchedData.DataType.ORIENTATION, firstCol, row - 1);

            // check if current is larger than last by at least maxOffset
            if ((current + offsets[row - fromY]) - (last + offsets[row - 1 - fromY]) > maxOffset) {
                while ((current + offsets[row - fromY]) - (last + offsets[row - 1 - fromY]) > maxOffset) {
                    offsets[row - fromY] -= PI;
                }
            } else { // current is smaller than last by at least maxOffset
                while ((current + offsets[row - fromY]) - (last + offsets[row - 1 - fromY]) < -maxOffset) {
                    offsets[row - fromY] += PI;
                }
            }
        }

        // fill first column
        for (int row = firstRow; row < toY; row++) {
            patch.set(PatchedData.DataType.ORIENTATION, firstCol, row, patch.get(PatchedData.DataType.ORIENTATION, firstCol, row) + offsets[row - fromY]);
        }

        // determine offsets for each row and fix the values
        for (int row = firstRow; row < toY; row++) {

            double offset = 0;

            for (int col = firstCol + 1; col < toX; col++) {
                double current = patch.get(PatchedData.DataType.ORIENTATION, col, row);
                double last = patch.get(PatchedData.DataType.ORIENTATION, col - 1, row);

                // check if current is larger than last by at least maxOffset
                if ((current + offset) - last > maxOffset) {
                    while ((current + offset) - last > maxOffset) {
                        offset -= PI;
                    }
                } else { // current is smaller than last by at least maxOffset
                    while ((current + offset) - last < -maxOffset) {
                        offset += PI;
                    }
                }

                patch.set(PatchedData.DataType.ORIENTATION, col, row, current + offset);
            }
        }
    }

    private void unwrapSpirals(PatchedData.Patch patch) {
        int padX = patch.getPadX();
        int padY = patch.getPadY();
        int fromX = patch.getFromX() - padX;
        int fromY = patch.getFromY() - padY;
        int toX = patch.getToX() + padX;
        int toY = patch.getToY() + padY;
        int w = toX - fromX;
        int h = toY - fromY;

        boolean[][] visited = new boolean[w][h];

        // the center coordinates
        int centerX = fromX + w / 2;
        int centerY = fromY + h / 2;

        // start at (centerX,centerY) and go through the data in a spiral

        int x = centerX;
        int y = centerY;

        // the direction in which to go: 0 = down, 1 = right, 2 = top, 3 = left
        int direction = 0;

        // the number of pixels to go into one direction before changing direction
        int distance = 1;

        // the distance that was already walked in the current direction
        int movedDistance = 0;

        // a simple switch to ensure that the distance is updated every two direction changes
        boolean distanceSwitch = false;

        // the number of visited pixels
        int visitedPixels = 1; // the center already counts as visited

        // the total offset that will be used to correct pixels
        double offset = 0;

        while (visitedPixels < w * h) {

            // update position (x,y)

            switch (direction) {
                case 0:
                    y += 1;
                    break;

                case 1:
                    x += 1;
                    break;

                case 2:
                    y -= 1;
                    break;

                case 3:
                    x -= 1;
                    break;
            }

            // the position has changed by one step, thus update the distance
            movedDistance++;

            // check if the max. distance in the current direction is reached and thus the direction must be updated
            if (movedDistance == distance) {
                // need to change the direction
                direction = (direction + 1) % 4;
                movedDistance = 0;

                if (!distanceSwitch) {
                    distanceSwitch = true;
                } else {
                    distance++;
                    distanceSwitch = false;
                }
            }

            // if the new position is outside the image, continue as usual without updating the visitedPixels counter
            // so that the spiral simply continues to grow but visits (a lot of) invalid pixels.
            // TODO: this strategy is quite inefficient and should be changed later
            if (!patch.isInPatchWithPadding(x, y)) {
                continue;
            }

            // check if valid pixel
            if (!patch.isInRoi(x, y) || Double.isNaN(patch.get(PatchedData.DataType.ORIENTATION, x, y))) {
                visitedPixels++;
                patch.set(PatchedData.DataType.ORIENTATION, x, y, Double.NaN);
                visited[x - fromX][y - fromY] = true;
                continue;
            }

            // calculate mean of neighbouring 8 pixels
            offset = unwrapOrientation(patch, offset, x, y, visited, fromX, fromY);

            // update the number of visited pixels
            visitedPixels++;

            visited[x - fromX][y - fromY] = true;
        }
    }

    private void unwrapDiamond(PatchedData.Patch patch) {
        int padX = patch.getPadX();
        int padY = patch.getPadY();
        int fromX = patch.getFromX() - padX;
        int fromY = patch.getFromY() - padY;
        int toX = patch.getToX() + padX;
        int toY = patch.getToY() + padY;
        int w = toX - fromX;
        int h = toY - fromY;

        boolean[][] visited = new boolean[w][h];

        // the center coordinates
        int centerX = fromX + w / 2;
        int centerY = fromY + h / 2;

        // the total offset that will be used to correct pixels
        double offset = 0;

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(centerX, centerY));

        while (!queue.isEmpty()) {
            Point p = queue.poll();

            int x = (int) p.getX();
            int y = (int) p.getY();

            if (!patch.isInPatchWithPadding(x, y) || visited[x - fromX][y - fromY]) {
                continue;
            }

            if (!patch.isInRoi(x, y)) {
                patch.set(PatchedData.DataType.ORIENTATION, x, y, Double.NaN);
                visited[x - fromX][y - fromY] = true;

                queue.add(new Point(x - 1, y));
                queue.add(new Point(x, y + 1));
                queue.add(new Point(x + 1, y));
                queue.add(new Point(x, y - 1));

                continue;
            }

            offset = unwrapOrientation(patch, offset, x, y, visited, fromX, fromY);

            visited[x - fromX][y - fromY] = true;

            queue.add(new Point(x - 1, y));
            queue.add(new Point(x, y + 1));
            queue.add(new Point(x + 1, y));
            queue.add(new Point(x, y - 1));
        }
    }

    private double unwrapOrientation(PatchedData.Patch patch, double offset, int x, int y, boolean[][] visited, int fromX, int fromY) {
        double reference = 0;
        double maxAbs = 0;
        double current = patch.get(PatchedData.DataType.ORIENTATION, x, y);

        int invalidPixels = 0;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if ((i == 0 && j == 0) || Double.isNaN(patch.get(PatchedData.DataType.ORIENTATION, x + i, y + j)) || !visited[x + i - fromX][y + j - fromY]) {
                    invalidPixels++;
                    continue;
                }
                double val = patch.get(PatchedData.DataType.ORIENTATION, x + i, y + j);
                if (abs(current + offset - reference) > maxAbs) {
                    reference = val;
                    maxAbs = abs(current + offset - reference);
                }
            }
        }

        if (invalidPixels == 9) {
            // if all neighbouring pixels are invalid, don't update
            return offset;
        }


        if ((current + offset) - reference > maxOffset) { // check if current is larger than reference by at least maxOffset
            while ((current + offset) - reference > maxOffset) {
                offset -= PI;
            }
        } else { // current is smaller than last by at least maxOffset
            while ((current + offset) - reference < -maxOffset) {
                offset += PI;
            }
        }

        patch.set(PatchedData.DataType.ORIENTATION, x, y, current + offset);
        return offset;
    }
}
