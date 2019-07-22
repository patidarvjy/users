package com.docutools.roles;

import com.docutools.users.Organisation;

import java.util.*;

/**
 * Privileges grant users the power to see specific data and execute specific actions in a project.
 */
public enum Privilege {
    ViewProjectSettings,
    ManageProjectSettings,

    ViewTeam,
    CreateTeamMembers,
    ManageTeam,

    ManagePlanFolders,
    CreatePlans,
    ManagePlans,

    ViewPins,
    CreatePins,
    ManagePins,

    ViewTasks,
    CreateTasks,
    ManageTasks,

    /**
     * Allows users to view dependencies of tasks assigned to them. Users with the {@link Privilege#ViewTasks} privilege
     * are allowed to see all tasks and there can always see dependencies.
     */
    ViewTaskDependencies,
    /**
     * Users without this privilege cannot close tasks assigned to them but set them to done, which leaves the task open
     * and the assigner has to close it manually.
     */
    CloseTasks,
    /**
     * Allows task assignees with now {@link Privilege#ManageTasks} privilege to delegate the task assignment to users
     * in the same {@link Organisation} in this project.
     */
    DelegateTasks,

    ViewComments,
    CreateComments,
    ManageComments,

    ViewMedia,
    CreateMedia,
    ManageMedia,

    ViewPhotos,
    CreatePhotos,

    ViewVideos,
    CreateVideos,

    ViewAudios,
    CreateAudios,

    ViewSketches,
    CreateSketches,

    ViewText,
    CreateText,

    ViewHistory,

    ViewDatasets,
    CreateDatasets,
    ManageDatasets,

    ViewGroups,
    CreateGroups,
    ManageGroups,

    ViewReports,
    CreateReports,
    ManageReports,

    RejectTasks;

    public static Set<Privilege> getPowerUserPrivileges(){
        return new HashSet<>(Arrays.asList(Privilege.values()));
    }

    public static Set<Privilege> getAssistantPrivileges(){
        return new HashSet<>(Arrays.asList(
                ViewProjectSettings,

                ViewTeam,
                CreateTeamMembers,

                CreatePlans,

                ViewPins,
                CreatePins,

                ViewTasks,
                CreateTasks,
                CloseTasks,
                DelegateTasks,

                ViewComments,
                CreateComments,
                ManageComments,

                ViewMedia,
                CreateMedia,

                ViewHistory,

                ViewDatasets,

                ViewGroups,
                CreateGroups,

                ViewReports,
                CreateReports
        ));
    }

    public static Set<Privilege> getViewerPrivileges(){
        return new HashSet<>(Arrays.asList(
                ViewProjectSettings,

                ViewTeam,

                ViewPins,

                ViewTasks,

                ViewComments,
                CreateComments,
                ManageComments,

                ViewMedia,

                ViewHistory,

                ViewDatasets,

                ViewGroups,

                ViewReports,
                CreateReports,
                ManageReports
        ));
    }
}
