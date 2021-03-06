package net.thegaminghuskymc.gadgetmod.programs.social_medias.task;

import net.thegaminghuskymc.gadgetmod.programs.email.ApplicationEmail.Email;
import net.thegaminghuskymc.gadgetmod.programs.email.ApplicationEmail.EmailManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.thegaminghuskymc.gadgetmod.api.task.Task;

import java.util.List;

public class TaskUpdateChannels extends Task {
    private List<Email> emails;

    public TaskUpdateChannels() {
        super("update_messages");
    }

    @Override
    public void prepareRequest(NBTTagCompound nbt) {
    }

    @Override
    public void processRequest(NBTTagCompound nbt, World world, EntityPlayer player) {
        this.emails = EmailManager.INSTANCE.getEmailsForAccount(player);
    }

    @Override
    public void prepareResponse(NBTTagCompound nbt) {
        NBTTagList tagList = new NBTTagList();
        if (emails != null) {
            for (Email email : emails) {
                NBTTagCompound emailTag = new NBTTagCompound();
                email.writeToNBT(emailTag);
                tagList.appendTag(emailTag);
            }
        }
        nbt.setTag("emails", tagList);
    }

    @Override
    public void processResponse(NBTTagCompound nbt) {
        EmailManager.INSTANCE.getInbox().clear();
        NBTTagList emails = (NBTTagList) nbt.getTag("emails");
        for (int i = 0; i < emails.tagCount(); i++) {
            NBTTagCompound emailTag = emails.getCompoundTagAt(i);
            Email email = Email.readFromNBT(emailTag);
            EmailManager.INSTANCE.getInbox().add(email);
        }
    }
}
