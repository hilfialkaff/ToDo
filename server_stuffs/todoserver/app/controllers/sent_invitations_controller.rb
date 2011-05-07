class SentInvitationsController < ApplicationController
  def create
    @sender = User.find(params[:user_id])
    @sent_inv = params[:sent_invitations]

    @receiver = User.find_by_name(params[:sent_invitation][:recipient])

    @receiver.recv_invitations.create(:group => params[:sent_invitation][:group], :description => params[:sent_invitation][:description], :sender => @sender.name)
    @created_inv = @sender.sent_invitations.new(:group => params[:sent_invitation][:group], :description => params[:sent_invitation][:description], :recipient => @receiver.name, :status => "Pending" )

    respond_to do |format|
      if @created_inv.save
        format.html { redirect_to(@created_inv, :notice => 'Invitation was successfully created.') }
        format.xml  { render :xml => @created_inv, :status => :created, :location => @created_inv }
        format.json  { render :json => @created_inv, :status => :created, :location => @created_inv }

      else
        format.html { render :action => "new" }
        format.xml  { render :xml => @created_inv.errors, :status => :unprocessable_entity }
        format.json  { render :json => @created_inv.errors, :status => :unprocessable_entity }
      end
    end
  end

  def destroy
    @user = User.find(params[:user_id])
    @sent_inv = @user.sent_invitations.find(params[:id])
    @sent_inv.destroy

    redirect_to user_path(@user)
  end

  def index
    @sent_invitations = User.find(params[:user_id]).sent_invitations.all

    respond_to do |format|
      format.xml { render :xml => @sent_invitations }
      format.json { render :json => @sent_invitations }
    end
  end
end
