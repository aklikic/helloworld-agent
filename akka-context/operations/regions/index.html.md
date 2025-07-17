<!-- <nav> -->
- [Akka](../../index.html)
- [Operating](../index.html)
- [Akka Automated Operations](../akka-platform.html)
- [Regions](index.html)

<!-- </nav> -->

# Regions

Projects in Akka can span across regions with data automatically replicated between all the regions. This increases availability as the regions can either be separate cloud / geographic regions or can be separate logical regions within the same cloud / geographic region. This gives you a high level of control for managing failure domains or fault boundaries in your applications. This is sometimes referred to as blast radius control.

[Regions](../organizations/regions.html) are specified in the project configuration. All services in the project are deployed to all regions. One of the regions will be specified as the primary region. The primary region indicates the source from which region resources (services, routes, secrets, etc.) should be replicated from. By default the primary region is the first one added to the project at deployment time.

Additionally, the primary region also indicates where primary data copies should reside in stateful components like Event Sourced Entities, Key Value Entities or Workflow when using the `pinned-region` primary selection mode.

|  | Regions appear at two different scopes in Akka. The first is at the [Organizations](../organizations/index.html) scope. This conveys which regions are available to your organization. The second is at the project scope, which conveys which regions a specific project is bound to. |
To see what regions have been configured for your project, you can run:

```command
akka project regions list
```

## <a href="about:blank#_adding_a_region_to_a_project"></a> Adding a region to a project

A region can be added to a project if the organization that owns the project has access to that region. To see which regions your organization has access to, run the `akka regions list` command:

```command
akka regions list --organization my-organization
```
To add one of these regions to your project, run:

```command
akka project regions add gcp-us-east1
```
When you deploy a service it will run in all regions of the project. When you add a region to a project the existing services will automatically start in the new region.

|  | Project region assignment is restricted to users that have `project-admin` or `superuser` role in the encompassing organization to which the project belongs, as documented in [Manage users](../organizations/manage-users.html). |

### <a href="about:blank#selecting-primary"></a> Selecting primary for stateful components

Stateful components like Event Sourced Entities and Workflows can be replicated to other regions. For each stateful component instance there is a primary region, which handles all write requests. Read requests can be served from any region. See [Event Sourced Entity replication](../../java/event-sourced-entities.html#_replication), [Key Value Entity replication](../../java/key-value-entities.html#_replication) and [Workflow replication](../../java/workflows.html#_replication) for more information about read and write requests.

There are two operational choices for deciding where the primary is located:

- **pinned-region** mode - one region is defined as the primary for the project, and all stateful component instances will use that region as primary
- **request-region** mode - the primary is selected by each individual component instance based on where the write requests occur

|  | Before changing the primary selection mode, make sure that you understand and follow the steps described in the [How to](about:blank#_how_to). |
The pinned-region mode is used by default. To use request-region mode you need to deploy the service with a [service descriptor](../services/deploy-service.html#apply):

```yaml
name: my-service
service:
  image: my-container-uri/container-name:tag-name
  replication:
    mode: replicated-read
    replicatedRead:
      primarySelectionMode: request-region
```
When using request-region mode, all regions must be available when the first write request is made to an entity or when switching primary region by handling a write request for an entity in another region than the currently selected primary region.

It is possible to switch between pinned-region and request-region mode, but this should only be done with careful consideration of the consequences. For example, when changing the primary, not all updates may have been replicated and the new primary may not be fully up to date. This is why there is a third mode. This is a read-only mode for all regions, which causes all write requests to be rejected. This can be used as an intermediate stage to ensure that all updates have been replicated before the primary is changed.

To use this read-only mode for all regions you set `primarySelectionMode` to `none` in the service descriptor:

```yaml
name: my-service
service:
  image: my-container-uri/container-name:tag-name
  replication:
    mode: replicated-read
    replicatedRead:
      primarySelectionMode: none
```
To use the pinned-region primary selection mode again you set `pinned-region` in the service descriptor:

```yaml
name: my-service
service:
  image: my-container-uri/container-name:tag-name
  replication:
    mode: replicated-read
    replicatedRead:
      primarySelectionMode: pinned-region
```

## <a href="about:blank#_setting_the_primary_region_of_a_project"></a> Setting the primary region of a project

Changing the primary region of a project is how you control failover or migration in Akka.

|  | The primary region of a project is also the region that will be used as primary for stateful components in the pinned-region selection mode. Changing primary should only be done with careful consideration of the consequences, and it is recommended to first change to the read-only mode in all regions. See [Selecting primary for stateful components](about:blank#selecting-primary). |

|  | Before changing the primary region, make sure that you understand and follow the steps described in the [How to](about:blank#_how_to). |
To change the primary region of a project run:

```command
akka project regions set-primary gcp-us-east1
```

|  | It may be necessary to clear the region cache when running the `akka` command on other machines before this change will be picked up. This can be done by running `akka config clear-cache`. |

## <a href="about:blank#multi-region-resources"></a> Managing resources in a multi region project

Akka projects are built to span regions. To accomplish this, Akka considers resources in two ways.

### <a href="about:blank#_global_resources"></a> Global resources

In an Akka project, services, routes, secrets, and observability configuration are all *global resources* in that they will deploy to all regions that the project is bound to.

The underlying replication mechanism is that when resources are deployed they first deploy to the primary region. Then a background process will asynchronously copy them to the remain regions. This background synchronization process is eventually consistent.

The `list` and `get` commands for multi-region resources display the sync status for global resources. These commands will show the resource in the primary region by default. You can specify which region you want to get the resource from by passing the `--region` flag. If you want to view the resource in all regions, you can pass the `--all-regions` flag.

### <a href="about:blank#_regional_resources"></a> Regional resources

There are certain circumstances where it may not be appropriate to have the same resource synced to all regions. Some common reasons are as follows:

- A route may need to be served from a different hostname in each region.
- A service may require different credentials for a third party service for each region, requiring a different secret to be deployed to each region.
- A different observability configuration may be needed in different regions, such that metrics and logs are aggregated locally in the region.
To deploy a resource as a regional resource, you can specify a `--region` flag to specify which region you want to create the resource in. When updating or deleting the resource, the `--region` flag needs to be passed.

### <a href="about:blank#_switching_between_global_and_regional_resources"></a> Switching between global and regional resources

If you have a global resource that you want to change to being a regional resource, this can be done by updating the resource, passing a `--region` flag, and passing the `--force-regional` flag to change it from a global to a regional resource. You must do this on the primary region first, otherwise the resource synchronization process may overwrite your changes.

If you have a regional resource that you want to change to being a global resource, this can be done by updating the resource without specifying a `--region` flag, but passing the `--force-global` flag instead. The command will perform the update in the primary region, and that configuration will be replicated to, and overwrite, the configuration in the rest of the regions.

## <a href="about:blank#_how_to"></a> How to

There can be several reasons for changing multi-region resources and the primary of stateful components. In this section we describe a few scenarios and provide a checklist of the recommended procedure.

### <a href="about:blank#_observe_replication_status"></a> Observe replication status

You can see throughput, lag, and errors in the replication section in the Control Tower. The replication lag is the time from when the events were created until they were received in the other region. Some errors may be normal, since the connections are sometimes restarted.

![dashboard control tower replication](../_images/dashboard-control-tower-replication.png)


### <a href="about:blank#_add_a_region"></a> Add a region

1. Follow the instructions in [Adding a region to a project](about:blank#_adding_a_region_to_a_project).
2. You have to [deploy the services](../services/deploy-service.html) again because the container images don’t exist in the container registry of the new region, unless you use a global container registry.
3. You need to [expose the services](../services/invoke-service.html) in the new region.
4. Stateful components are automatically replicated to the new region. This may take some time, and you can see progress in the replication section in the Control Tower. The event consumption lag will at first be high and then close to zero when the replication has been completed.

### <a href="about:blank#_switch_from_pinned_region_to_request_region_primary_selection_mode_for_stateful_components"></a> Switch from pinned-region to request-region primary selection mode for stateful components

The default primary selection mode for stateful components is the pinned-region mode, as explained in [Selecting primary for stateful components](about:blank#selecting-primary), and you might want to change that to request-region after the first deployment. That section also describes how you change the primary selection mode with a service descriptor.

Component instances that have already been created will continue to have their primary in the original pinned-region primary region, and will switch primary region when write requests occur in the other region(s).

1. First, change to the `none` primary selection mode. This is a read-only mode for all regions and all write requests will be rejected. The reason for changing to this intermediate mode is to make sure that all events have been replicated without creating new events.
2. Wait until the deployment of the `none` primary selection mode has been successfully propagated to all regions. Observe in the Akka Console that the rolling update has been completed in all regions. You can also make sure that replicated events reach zero in the replication section in the Control Tower.
3. Change to `request-region` primary selection mode.

### <a href="about:blank#_switch_from_request_region_to_pinned_region_primary_selection_mode_for_stateful_components"></a> Switch from request-region to pinned-region primary selection mode for stateful components

pinned-region mode takes precedence over request-region in the sense that a component instance will change its primary to the pinned-region region when there is a new write request to the component instance, and it persists a new event.

[Selecting primary for stateful components](about:blank#selecting-primary) describes how you change the primary selection mode with a service descriptor.

1. First, change to the `none` primary selection mode. This is a read-only mode for all regions and all write requests will be rejected. The reason for changing to this intermediate mode is to make sure that all events have been replicated without creating new events.
2. Wait until the deployment of the `none` primary selection mode has been successfully propagated to all regions. Observe in the Akka Console that the rolling update has been completed in all regions. You can also make sure that replicated events reach zero in the replication section in the Control Tower.
3. Change to `pinned-region` primary selection mode.

### <a href="about:blank#_change_the_pinned_region_primary_region_for_stateful_components"></a> Change the pinned-region primary region for stateful components

You might want to change the pinned-region primary for stateful components if you migrate from one region to another, or need to bring down the primary region for maintenance for a while.

[Selecting primary for stateful components](about:blank#selecting-primary) describes how you change the primary selection mode with a service descriptor.

1. First, change to the `none` primary selection mode. This is a read-only mode for all regions and all write requests will be rejected. The reason for changing to this intermediate mode is to make sure that all events have been replicated without creating new events.
2. Wait until the deployment of the `none` primary selection mode has been successfully propagated to all regions. Observe in the Akka Console that the rolling update has been completed in all regions. You can also make sure that replicated events reach zero in the replication section in the Control Tower.
3. Follow instructions in [Setting the primary region of a project](about:blank#_setting_the_primary_region_of_a_project).
4. Change to `pinned-region` primary selection mode.

### <a href="about:blank#_change_primary_region_for_disaster_recovery"></a> Change primary region for disaster recovery

If a region is failing you might want to fail over to another region that is working.

1. If the failing region is the primary region, follow instructions in [Setting the primary region of a project](about:blank#_setting_the_primary_region_of_a_project) and change the primary to a non-failing region.
2. If you are using `request-region` primary selection you should [Switch from request-region to pinned-region primary selection mode for stateful components](about:blank#_switch_from_request_region_to_pinned_region_primary_selection_mode_for_stateful_components). Depending on how responsive the failing region is this might not be possible to deploy to the failing region, but you should deploy it to the non-failing regions. The reason for this is that otherwise write requests will still be routed to the failing region for component instances that have their primary in the failing region.
3. Be aware that events that were written in the failing region and had not been replicated to other regions before the hard failover will be replicated when the regions are connected again. There are no guarantees regarding the order of these "old" events and any new events written by the new primary, which could lead to conflicting states across regions.
For faster fail over you can consider the alternative of [Fast downing of region for disaster recovery](about:blank#_fast_downing_of_region_for_disaster_recovery), but the drawback is that is more difficult to recover the failed region.

### <a href="about:blank#_fast_downing_of_region_for_disaster_recovery"></a> Fast downing of region for disaster recovery

If the communication with a region is failing or it is completely unresponsive, you might want to take out the failing region, without re-deploying the services in the healthy regions.

1. Use `down-region` from the CLI:

```command
akka project settings down-region gcp-us-east1 aws-us-east-2 --region aws-us-east-2
```
In the above example, `gcp-us-east1` is the failed region that is downed. `aws-us-east-2` is selected as new `pinned-region`, and the CLI command is sent to `aws-us-east-2`.
2. You can send the same command to the failed region, but it will probably not be able to receive it, and that is fine.

```command
akka project settings down-region gcp-us-east1 aws-us-east-2 --region gcp-us-east1
```
3. If you have more than two regions you should send the same command to all other regions using the `--region` flag.
4. You should try to stop the services in the downed region, if they are still running.

```command
akka service pause my-service --region gcp-us-east1
```
5. Contact Akka support for options of how to recover the failed and downed region.

<!-- <footer> -->
<!-- <nav> -->
[Data migration](../services/data-management.html) [TLS certificates](../tls-certificates.html)
<!-- </nav> -->

<!-- </footer> -->

<!-- <aside> -->

<!-- </aside> -->