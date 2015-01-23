package ReFlow::StepClasses::RunAndMonitorDistribJob;

@ISA = (ReFlow::Controller::WorkflowStepHandle);

use strict;
use ReFlow::Controller::WorkflowStepHandle;
use File::Basename;

sub run {
  my ($self, $test, $undo) = @_;

  my $clusterServer = $self->getSharedConfig('clusterServer');

  # get parameters
  my $taskInputDir = $self->getParamValue("taskInputDir");
  my $numNodes = $self->getParamValue("numNodes");
  my $maxMemoryGigs = $self->getConfig("maxMemoryGigs", 1);
  $maxMemoryGigs = $self->getParamValue("maxMemoryGigs") unless $maxMemoryGigs;
  my $processorsPerNode = $self->getParamValue("processorsPerNode");

  $processorsPerNode = 1 unless $processorsPerNode;

  # get global properties
  my $clusterQueue = $self->getSharedConfig("$clusterServer.clusterQueue");

  my $clusterTaskLogsDir = $self->getDistribJobLogsDir();
  my $clusterDataDir = $self->getClusterWorkflowDataDir();

  my $userName = $self->getSharedConfig("$clusterServer.clusterLogin");


  my $propFile = "$clusterDataDir/$taskInputDir/controller.prop";
  my $jobInfoFile = "$clusterDataDir/$taskInputDir/distribjobJobInfo.txt";
  my $logFile = "$clusterTaskLogsDir/" . $self->getName() . ".log";

  if($undo){
      my ($relativeTaskInputDir, $relativeDir) = fileparse($taskInputDir);
      $self->runCmdOnCluster(0, "rm -fr $clusterDataDir/$relativeDir/master");
      $self->runCmdOnCluster(0, "rm -fr $clusterDataDir/$relativeDir/input/subtasks");
      $self->runCmdOnCluster(0, "rm -fr $logFile");
  }else{
      my $expectedTime = 0;  # don't provide any
      my $success=$self->runAndMonitorDistribJob($test, $userName, $clusterServer, $jobInfoFile, $logFile, $propFile, $numNodes, $expectedTime, $clusterQueue, $processorsPerNode, $maxMemoryGigs);
      my $masterDir = $propFile;
      $masterDir =~ s|/input/.*|/master|;  # remove all of the path after input/ and change it to master
      if (!$success){
	  $self->error (
"
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
The cluster task did not successfully run. Check the task log file on the cluster: 
  $logFile

If the task log file ends in a perl error, that suggests an unusual distribjob controller failure.  Often those are recoverable by setting the step to ready and trying again.

Otherwise, to diagnose the problem, look in the task log to see what command is executed on the nodes, and also see the logs of the individual failed subtasks.  Find those logs at
  $masterDir/failures/subtask_XXXX/result/slot/

(where XXXX is a failed subtask number)
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
");
      }
  }
}

sub getConfigDeclaration {
  return (
         # [name, default, description]
         # ['', '', ''],
         );
}

