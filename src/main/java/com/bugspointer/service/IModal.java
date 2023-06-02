package com.bugspointer.service;

import com.bugspointer.dto.ModalDTO;
import com.bugspointer.dto.Response;

public interface IModal {

    Response saveModal(ModalDTO dto);
}
