class TododetailsController < ApplicationController
  # GET /tododetails
  # GET /tododetails.xml
  def index
    @tododetails = Group.find(params[:group_id]).tododetails 

    respond_to do |format|
      format.html # index.html.erb
      format.xml  { render :xml => @tododetails }
      format.json { render :json => @tododetail }
    end
  end

  # GET /tododetails/1
  # GET /tododetails/1.xml
  def show
    @tododetail = Tododetail.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.xml  { render :xml => @tododetail }
      format.json { render :json => @tododetail }
    end
  end

  # GET /tododetails/new
  # GET /tododetails/new.xml
  def new
    @tododetail = Tododetail.new

    respond_to do |format|
      format.html # new.html.erb
      format.xml  { render :xml => @tododetail }
      format.json { render :json => @tododetail }
    end
  end

  # GET /tododetails/1/edit
  def edit
    @tododetail = Tododetail.find(params[:id])
  end

  # POST /tododetails
  # POST /tododetails.xml
  def create
    @group = Group.find(params[:group_id])
    @tododetail = @group.tododetails.create(params[:tododetail])

    respond_to do |format|
      format.html { redirect_to(@group, :notice => 'Tododetail was successfully created.') }
      format.xml  { render :xml => @tododetail, :status => :created, :location => @group }
      format.json  { render :json => @tododetail, :status => :created, :location => @group }

      if true == false:
        format.html { render :action => "new" }
        format.xml  { render :xml => @tododetail.errors, :status => :unprocessable_entity }
        format.json  { render :json => @tododetail.errors, :status => :unprocessable_entity }
      end
    end
  end

  # PUT /tododetails/1
  # PUT /tododetails/1.xml
  def update
    @tododetail = Tododetail.find(params[:id])

    respond_to do |format|
      if @tododetail.update_attributes(params[:tododetail])
        format.html { redirect_to(@tododetail, :notice => 'Tododetail was successfully updated.') }
        format.xml  { head :ok }
        format.json { head :ok }
      else
        format.html { render :action => "edit" }
        format.xml  { render :xml => @tododetail.errors, :status => :unprocessable_entity }
	format.json { render :json => @tododetail.errors, :status => :unprocessable_entity }
      end
    end
  end

  # DELETE /tododetails/1
  # DELETE /tododetails/1.xml
  def destroy
    @group = Group.find(params[:group_id])
    @tododetail = @group.tododetails.find(params[:id])
    @tododetail.destroy

    respond_to do |format|
      format.html { redirect_to(@group) }
      format.xml  { head :ok }
      format.json { head :ok }
    end
  end
end
