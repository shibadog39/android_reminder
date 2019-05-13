package com.asahina.test.view.utils

/**
 * variableId と layoutId のペア。
 */
class VariableLayoutPair {

    constructor(variableId: Int, layoutId: Int) {
        this.variableId = variableId
        this.layoutId = layoutId
    }

    val variableId: Int

    val layoutId: Int
}