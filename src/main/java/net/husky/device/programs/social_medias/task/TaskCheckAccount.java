package net.husky.device.programs.social_medias.task;

import net.husky.device.api.task.Task;
import net.husky.device.programs.email.ApplicationEmail.EmailManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class TaskCheckAccount extends Task
{
	private boolean hasAccount = false;
	private String name = null;
	
	public TaskCheckAccount()
	{
		super("check_account");
	}

	@Override
	public void prepareRequest(NBTTagCompound nbt) {}

	@Override
	public void processRequest(NBTTagCompound nbt, World world, EntityPlayer player) 
	{
		this.hasAccount = EmailManager.INSTANCE.hasAccount(player.getUniqueID());
		if(this.hasAccount)
		{
			this.name = EmailManager.INSTANCE.getName(player);
			this.setSuccessful();
		}
	}

	@Override
	public void prepareResponse(NBTTagCompound nbt) 
	{
		if(this.isSucessful()) nbt.setString("Name", this.name);
	}

	@Override
	public void processResponse(NBTTagCompound nbt) {}

}