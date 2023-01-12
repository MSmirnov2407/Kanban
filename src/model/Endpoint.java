package model;

/**
 * Перечисление, в котором хранятся типы эндпоинтов для HttpTaskServer
 */

public enum Endpoint {
    GET_TASK_LIST,
    GET_TASK_BY_ID,
    ADD_OR_UPDATE_TASK,
    DELETE_TASK_BY_ID,
    DELETE_ALL_TASKS,
    GET_SUBTASK_LIST,
    GET_SUBTASK_BY_ID,
    ADD_OR_UPDATE_SUBTASK,
    DELETE_SUBTASK_BY_ID,
    DELETE_ALL_SUBTASKS,
    GET_EPIC_LIST,
    GET_EPIC_BY_ID,
    ADD_OR_UPDATE_EPIC,
    DELETE_EPIC_BY_ID,
    DELETE_ALL_EPICS,
    GET_EPIC_SUBTASK_BY_ID,
    GET_HISTORY,
    GET_PRIORITIZED_TASKS,
    UNKNOWN
}
