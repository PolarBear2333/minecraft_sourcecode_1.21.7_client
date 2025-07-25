/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class LecternBlockEntity
extends BlockEntity
implements Clearable,
MenuProvider {
    public static final int DATA_PAGE = 0;
    public static final int NUM_DATA = 1;
    public static final int SLOT_BOOK = 0;
    public static final int NUM_SLOTS = 1;
    private final Container bookAccess = new Container(){

        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return LecternBlockEntity.this.book.isEmpty();
        }

        @Override
        public ItemStack getItem(int n) {
            return n == 0 ? LecternBlockEntity.this.book : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int n, int n2) {
            if (n == 0) {
                ItemStack itemStack = LecternBlockEntity.this.book.split(n2);
                if (LecternBlockEntity.this.book.isEmpty()) {
                    LecternBlockEntity.this.onBookItemRemove();
                }
                return itemStack;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int n) {
            if (n == 0) {
                ItemStack itemStack = LecternBlockEntity.this.book;
                LecternBlockEntity.this.book = ItemStack.EMPTY;
                LecternBlockEntity.this.onBookItemRemove();
                return itemStack;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int n, ItemStack itemStack) {
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            LecternBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return Container.stillValidBlockEntity(LecternBlockEntity.this, player) && LecternBlockEntity.this.hasBook();
        }

        @Override
        public boolean canPlaceItem(int n, ItemStack itemStack) {
            return false;
        }

        @Override
        public void clearContent() {
        }
    };
    private final ContainerData dataAccess = new ContainerData(){

        @Override
        public int get(int n) {
            return n == 0 ? LecternBlockEntity.this.page : 0;
        }

        @Override
        public void set(int n, int n2) {
            if (n == 0) {
                LecternBlockEntity.this.setPage(n2);
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };
    ItemStack book = ItemStack.EMPTY;
    int page;
    private int pageCount;

    public LecternBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.LECTERN, blockPos, blockState);
    }

    public ItemStack getBook() {
        return this.book;
    }

    public boolean hasBook() {
        return this.book.has(DataComponents.WRITABLE_BOOK_CONTENT) || this.book.has(DataComponents.WRITTEN_BOOK_CONTENT);
    }

    public void setBook(ItemStack itemStack) {
        this.setBook(itemStack, null);
    }

    void onBookItemRemove() {
        this.page = 0;
        this.pageCount = 0;
        LecternBlock.resetBookState(null, this.getLevel(), this.getBlockPos(), this.getBlockState(), false);
    }

    public void setBook(ItemStack itemStack, @Nullable Player player) {
        this.book = this.resolveBook(itemStack, player);
        this.page = 0;
        this.pageCount = LecternBlockEntity.getPageCount(this.book);
        this.setChanged();
    }

    void setPage(int n) {
        int n2 = Mth.clamp(n, 0, this.pageCount - 1);
        if (n2 != this.page) {
            this.page = n2;
            this.setChanged();
            LecternBlock.signalPageChange(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public int getPage() {
        return this.page;
    }

    public int getRedstoneSignal() {
        float f = this.pageCount > 1 ? (float)this.getPage() / ((float)this.pageCount - 1.0f) : 1.0f;
        return Mth.floor(f * 14.0f) + (this.hasBook() ? 1 : 0);
    }

    private ItemStack resolveBook(ItemStack itemStack, @Nullable Player player) {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            WrittenBookContent.resolveForItem(itemStack, this.createCommandSourceStack(player, serverLevel), player);
        }
        return itemStack;
    }

    private CommandSourceStack createCommandSourceStack(@Nullable Player player, ServerLevel serverLevel) {
        Component component;
        String string;
        if (player == null) {
            string = "Lectern";
            component = Component.literal("Lectern");
        } else {
            string = player.getName().getString();
            component = player.getDisplayName();
        }
        Vec3 vec3 = Vec3.atCenterOf(this.worldPosition);
        return new CommandSourceStack(CommandSource.NULL, vec3, Vec2.ZERO, serverLevel, 2, string, component, serverLevel.getServer(), player);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.book = valueInput.read("Book", ItemStack.CODEC).map(itemStack -> this.resolveBook((ItemStack)itemStack, null)).orElse(ItemStack.EMPTY);
        this.pageCount = LecternBlockEntity.getPageCount(this.book);
        this.page = Mth.clamp(valueInput.getIntOr("Page", 0), 0, this.pageCount - 1);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        if (!this.getBook().isEmpty()) {
            valueOutput.store("Book", ItemStack.CODEC, this.getBook());
            valueOutput.putInt("Page", this.page);
        }
    }

    @Override
    public void clearContent() {
        this.setBook(ItemStack.EMPTY);
    }

    @Override
    public void preRemoveSideEffects(BlockPos blockPos, BlockState blockState) {
        if (blockState.getValue(LecternBlock.HAS_BOOK).booleanValue() && this.level != null) {
            Direction direction = blockState.getValue(LecternBlock.FACING);
            ItemStack itemStack = this.getBook().copy();
            float f = 0.25f * (float)direction.getStepX();
            float f2 = 0.25f * (float)direction.getStepZ();
            ItemEntity itemEntity = new ItemEntity(this.level, (double)blockPos.getX() + 0.5 + (double)f, blockPos.getY() + 1, (double)blockPos.getZ() + 0.5 + (double)f2, itemStack);
            itemEntity.setDefaultPickUpDelay();
            this.level.addFreshEntity(itemEntity);
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int n, Inventory inventory, Player player) {
        return new LecternMenu(n, this.bookAccess, this.dataAccess);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.lectern");
    }

    private static int getPageCount(ItemStack itemStack) {
        WrittenBookContent writtenBookContent = itemStack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (writtenBookContent != null) {
            return writtenBookContent.pages().size();
        }
        WritableBookContent writableBookContent = itemStack.get(DataComponents.WRITABLE_BOOK_CONTENT);
        if (writableBookContent != null) {
            return writableBookContent.pages().size();
        }
        return 0;
    }
}

