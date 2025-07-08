/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

public class Octree {
    private final Branch root;
    final BlockPos cameraSectionCenter;

    public Octree(SectionPos sectionPos, int n, int n2, int n3) {
        int n4 = n * 2 + 1;
        int n5 = Mth.smallestEncompassingPowerOfTwo(n4);
        int n6 = n * 16;
        BlockPos blockPos = sectionPos.origin();
        this.cameraSectionCenter = sectionPos.center();
        int n7 = blockPos.getX() - n6;
        int n8 = n7 + n5 * 16 - 1;
        int n9 = n5 >= n2 ? n3 : blockPos.getY() - n6;
        int n10 = n9 + n5 * 16 - 1;
        int n11 = blockPos.getZ() - n6;
        int n12 = n11 + n5 * 16 - 1;
        this.root = new Branch(new BoundingBox(n7, n9, n11, n8, n10, n12));
    }

    public boolean add(SectionRenderDispatcher.RenderSection renderSection) {
        return this.root.add(renderSection);
    }

    public void visitNodes(OctreeVisitor octreeVisitor, Frustum frustum, int n) {
        this.root.visitNodes(octreeVisitor, false, frustum, 0, n, true);
    }

    boolean isClose(double d, double d2, double d3, double d4, double d5, double d6, int n) {
        int n2 = this.cameraSectionCenter.getX();
        int n3 = this.cameraSectionCenter.getY();
        int n4 = this.cameraSectionCenter.getZ();
        return (double)n2 > d - (double)n && (double)n2 < d4 + (double)n && (double)n3 > d2 - (double)n && (double)n3 < d5 + (double)n && (double)n4 > d3 - (double)n && (double)n4 < d6 + (double)n;
    }

    class Branch
    implements Node {
        private final Node[] nodes = new Node[8];
        private final BoundingBox boundingBox;
        private final int bbCenterX;
        private final int bbCenterY;
        private final int bbCenterZ;
        private final AxisSorting sorting;
        private final boolean cameraXDiffNegative;
        private final boolean cameraYDiffNegative;
        private final boolean cameraZDiffNegative;

        public Branch(BoundingBox boundingBox) {
            this.boundingBox = boundingBox;
            this.bbCenterX = this.boundingBox.minX() + this.boundingBox.getXSpan() / 2;
            this.bbCenterY = this.boundingBox.minY() + this.boundingBox.getYSpan() / 2;
            this.bbCenterZ = this.boundingBox.minZ() + this.boundingBox.getZSpan() / 2;
            int n = Octree.this.cameraSectionCenter.getX() - this.bbCenterX;
            int n2 = Octree.this.cameraSectionCenter.getY() - this.bbCenterY;
            int n3 = Octree.this.cameraSectionCenter.getZ() - this.bbCenterZ;
            this.sorting = AxisSorting.getAxisSorting(Math.abs(n), Math.abs(n2), Math.abs(n3));
            this.cameraXDiffNegative = n < 0;
            this.cameraYDiffNegative = n2 < 0;
            this.cameraZDiffNegative = n3 < 0;
        }

        public boolean add(SectionRenderDispatcher.RenderSection renderSection) {
            long l = renderSection.getSectionNode();
            boolean bl = SectionPos.sectionToBlockCoord(SectionPos.x(l)) - this.bbCenterX < 0;
            boolean bl2 = SectionPos.sectionToBlockCoord(SectionPos.y(l)) - this.bbCenterY < 0;
            boolean bl3 = SectionPos.sectionToBlockCoord(SectionPos.z(l)) - this.bbCenterZ < 0;
            boolean bl4 = bl != this.cameraXDiffNegative;
            boolean bl5 = bl2 != this.cameraYDiffNegative;
            boolean bl6 = bl3 != this.cameraZDiffNegative;
            int n = Branch.getNodeIndex(this.sorting, bl4, bl5, bl6);
            if (this.areChildrenLeaves()) {
                boolean bl7 = this.nodes[n] != null;
                this.nodes[n] = new Leaf(renderSection);
                return !bl7;
            }
            if (this.nodes[n] != null) {
                Branch branch = (Branch)this.nodes[n];
                return branch.add(renderSection);
            }
            BoundingBox boundingBox = this.createChildBoundingBox(bl, bl2, bl3);
            Branch branch = new Branch(boundingBox);
            this.nodes[n] = branch;
            return branch.add(renderSection);
        }

        private static int getNodeIndex(AxisSorting axisSorting, boolean bl, boolean bl2, boolean bl3) {
            int n = 0;
            if (bl) {
                n += axisSorting.xShift;
            }
            if (bl2) {
                n += axisSorting.yShift;
            }
            if (bl3) {
                n += axisSorting.zShift;
            }
            return n;
        }

        private boolean areChildrenLeaves() {
            return this.boundingBox.getXSpan() == 32;
        }

        private BoundingBox createChildBoundingBox(boolean bl, boolean bl2, boolean bl3) {
            int n;
            int n2;
            int n3;
            int n4;
            int n5;
            int n6;
            if (bl) {
                n6 = this.boundingBox.minX();
                n5 = this.bbCenterX - 1;
            } else {
                n6 = this.bbCenterX;
                n5 = this.boundingBox.maxX();
            }
            if (bl2) {
                n4 = this.boundingBox.minY();
                n3 = this.bbCenterY - 1;
            } else {
                n4 = this.bbCenterY;
                n3 = this.boundingBox.maxY();
            }
            if (bl3) {
                n2 = this.boundingBox.minZ();
                n = this.bbCenterZ - 1;
            } else {
                n2 = this.bbCenterZ;
                n = this.boundingBox.maxZ();
            }
            return new BoundingBox(n6, n4, n2, n5, n3, n);
        }

        @Override
        public void visitNodes(OctreeVisitor octreeVisitor, boolean bl, Frustum frustum, int n, int n2, boolean bl2) {
            boolean bl3 = bl;
            if (!bl) {
                int n3 = frustum.cubeInFrustum(this.boundingBox);
                bl = n3 == -2;
                boolean bl4 = bl3 = n3 == -2 || n3 == -1;
            }
            if (bl3) {
                bl2 = bl2 && Octree.this.isClose(this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ(), this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ(), n2);
                octreeVisitor.visit(this, bl, n, bl2);
                for (Node node : this.nodes) {
                    if (node == null) continue;
                    node.visitNodes(octreeVisitor, bl, frustum, n + 1, n2, bl2);
                }
            }
        }

        @Override
        @Nullable
        public SectionRenderDispatcher.RenderSection getSection() {
            return null;
        }

        @Override
        public AABB getAABB() {
            return new AABB(this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ(), this.boundingBox.maxX() + 1, this.boundingBox.maxY() + 1, this.boundingBox.maxZ() + 1);
        }
    }

    @FunctionalInterface
    public static interface OctreeVisitor {
        public void visit(Node var1, boolean var2, int var3, boolean var4);
    }

    static enum AxisSorting {
        XYZ(4, 2, 1),
        XZY(4, 1, 2),
        YXZ(2, 4, 1),
        YZX(1, 4, 2),
        ZXY(2, 1, 4),
        ZYX(1, 2, 4);

        final int xShift;
        final int yShift;
        final int zShift;

        private AxisSorting(int n2, int n3, int n4) {
            this.xShift = n2;
            this.yShift = n3;
            this.zShift = n4;
        }

        public static AxisSorting getAxisSorting(int n, int n2, int n3) {
            if (n > n2 && n > n3) {
                if (n2 > n3) {
                    return XYZ;
                }
                return XZY;
            }
            if (n2 > n && n2 > n3) {
                if (n > n3) {
                    return YXZ;
                }
                return YZX;
            }
            if (n > n2) {
                return ZXY;
            }
            return ZYX;
        }
    }

    public static interface Node {
        public void visitNodes(OctreeVisitor var1, boolean var2, Frustum var3, int var4, int var5, boolean var6);

        @Nullable
        public SectionRenderDispatcher.RenderSection getSection();

        public AABB getAABB();
    }

    final class Leaf
    implements Node {
        private final SectionRenderDispatcher.RenderSection section;

        Leaf(SectionRenderDispatcher.RenderSection renderSection) {
            this.section = renderSection;
        }

        @Override
        public void visitNodes(OctreeVisitor octreeVisitor, boolean bl, Frustum frustum, int n, int n2, boolean bl2) {
            AABB aABB = this.section.getBoundingBox();
            if (bl || frustum.isVisible(this.getSection().getBoundingBox())) {
                bl2 = bl2 && Octree.this.isClose(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, n2);
                octreeVisitor.visit(this, bl, n, bl2);
            }
        }

        @Override
        public SectionRenderDispatcher.RenderSection getSection() {
            return this.section;
        }

        @Override
        public AABB getAABB() {
            return this.section.getBoundingBox();
        }
    }
}

