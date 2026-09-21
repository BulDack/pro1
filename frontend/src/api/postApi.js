import axios from "axios";

const API = "/api/posts";

export const getPosts = async (page = 0, sort = "createdDate,desc") => {
    const response = await axios.get(API, {
        params: {
            page,
            size: 10,
            sort
        }
    });

    return response.data;
};

export const getPost = async (id) => {
    const response = await axios.get(`${API}/${id}`);

    return response.data;
};

export const createPost = async (data) => {
    const response = await axios.post(API, data);

    return response.data;
};

export const updatePost = async (id, data) => {
    await axios.put(`${API}/${id}`, data);
};

export const deletePost = async (id) => {
    await axios.delete(`${API}/${id}`);
};